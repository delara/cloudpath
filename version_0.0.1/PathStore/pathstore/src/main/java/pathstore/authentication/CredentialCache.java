package pathstore.authentication;

import com.datastax.driver.core.Session;
import lombok.Getter;
import lombok.NonNull;
import pathstore.authentication.datalayerimpls.LocalAuth;
import pathstore.authentication.datalayerimpls.LocalClientAuth;
import pathstore.system.PathStorePrivilegedCluster;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is used to cache all credentials in the pathstore_appliactions.local_auth table into
 * memory.
 *
 * <p>All writes to that table should be done through this class if they're related to this node
 * specifically.
 *
 * @see pathstore.client.PathStoreCluster
 * @see PathStorePrivilegedCluster
 */
public final class CredentialCache<SearchableT, CredentialT extends Credential<SearchableT>> {

  /** Instance of the node cache */
  @Getter(lazy = true)
  private static final CredentialCache<Integer, NodeCredential> nodeAuth =
      new CredentialCache<>(new LocalAuth());

  /** Instance of the client auth cache */
  @Getter(lazy = true)
  private static final CredentialCache<String, ClientCredential> clientAuth =
      new CredentialCache<>(new LocalClientAuth());

  /** Session used to modify the local database. */
  private final Session privSession = PathStorePrivilegedCluster.getSuperUserInstance().connect();

  /**
   * How to perform database operations on the given credential type
   *
   * @apiNote This is public because {@link
   *     pathstore.system.deployment.commands.WriteCredentialsToChildNode}
   */
  public final CredentialDataLayer<SearchableT, CredentialT> credentialDataLayer;

  /** Internal cache of credentials based on node id */
  private final ConcurrentMap<SearchableT, CredentialT> credentials;

  /**
   * Loads all existing credentials from the local table into memory
   *
   * @param credentialDataLayer how to readAndWrite
   */
  private CredentialCache(final CredentialDataLayer<SearchableT, CredentialT> credentialDataLayer) {
    this.credentialDataLayer = credentialDataLayer;
    this.credentials = this.credentialDataLayer.load(this.privSession);
  }

  /** @return get a reference to all credentials */
  public Collection<CredentialT> getAllReference() {
    return this.credentials.values();
  }

  /** @param credential credential to add to the cache */
  public void add(@NonNull final CredentialT credential) {
    this.credentials.put(
        credential.getSearchable(), this.credentialDataLayer.write(this.privSession, credential));
  }

  /**
   * This will remove a specific node id from the internal map and from the table
   *
   * @param primaryKey node id to remove
   * @return true if deletion occurred
   */
  public boolean remove(final SearchableT primaryKey) {
    CredentialT credential = this.getCredential(primaryKey);

    if (credential == null) return false;

    this.credentials.remove(primaryKey);

    this.credentialDataLayer.delete(this.privSession, credential);

    return true;
  }

  /**
   * @param primaryKey node id to get credential from
   * @return credential object, may be null
   */
  public CredentialT getCredential(final SearchableT primaryKey) {
    return this.credentials.get(primaryKey);
  }
}
