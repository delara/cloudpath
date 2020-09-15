package pathstorestartup.commands;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import pathstore.common.Constants;
import pathstore.system.PathStorePrivilegedCluster;
import pathstore.system.deployment.commands.ICommand;
import pathstore.system.deployment.deploymentFSM.DeploymentProcessStatus;
import pathstore.system.deployment.deploymentFSM.ServerAuthType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;

import static pathstore.common.Constants.DEPLOYMENT_COLUMNS.*;
import static pathstore.common.Constants.DEPLOYMENT_COLUMNS.WAIT_FOR;
import static pathstore.common.Constants.PATHSTORE_META_COLUMNS.*;
import static pathstore.common.Constants.SERVERS_COLUMNS.*;
import static pathstore.common.Constants.SERVERS_COLUMNS.SERVER_UUID;

/**
 * This command will write the server record for the root node and the deployment record for the
 * root node
 */
public class FinalizeRootInstallation implements ICommand {

  /** Cassandra account username */
  private final String cassandraUsername;

  /** Cassandra account password */
  private final String cassandraPassword;

  /** Ip of root node */
  private final String ip;

  /** Cassandra port for root node */
  private final int cassandraPort;

  /** Username to server */
  private final String username;

  /** Password to server */
  private final String password;

  /** Ssh port to server */
  private final int sshPort;

  /** Rmi port to server */
  private final int rmiPort;

  /**
   * @param cassandraUsername {@link #cassandraUsername}
   * @param cassandraPassword {@link #cassandraPassword}
   * @param ip {@link #ip}
   * @param cassandraPort {@link #cassandraPort}
   * @param username {@link #username}
   * @param password {@link #password}
   * @param sshPort {@link #sshPort}
   * @param rmiPort {@link #rmiPort}
   */
  public FinalizeRootInstallation(
      final String cassandraUsername,
      final String cassandraPassword,
      final String ip,
      final int cassandraPort,
      final String username,
      final String password,
      final int sshPort,
      final int rmiPort) {
    this.cassandraUsername = cassandraUsername;
    this.cassandraPassword = cassandraPassword;
    this.ip = ip;
    this.cassandraPort = cassandraPort;
    this.username = username;
    this.password = password;
    this.sshPort = sshPort;
    this.rmiPort = rmiPort;
  }

  /**
   * This command will write the root node server record to the table and write the root node
   * deployment record
   */
  @Override
  public void execute() {

    PathStorePrivilegedCluster cluster =
        PathStorePrivilegedCluster.getChildInstance(
            this.cassandraUsername, this.cassandraPassword, this.ip, this.cassandraPort);
    Session session = cluster.connect();

    UUID serverUUID = UUID.randomUUID();

    Insert insert =
        QueryBuilder.insertInto(Constants.PATHSTORE_APPLICATIONS, Constants.SERVERS)
            .value(PATHSTORE_VERSION, QueryBuilder.now())
            .value(PATHSTORE_PARENT_TIMESTAMP, QueryBuilder.now())
            .value(PATHSTORE_DIRTY, true)
            .value(SERVER_UUID, serverUUID.toString())
            .value(IP, ip)
            .value(USERNAME, username)
            .value(AUTH_TYPE, ServerAuthType.PASSWORD.toString()) // TODO Myles: Temporary
            .value(PASSWORD, password)
            .value(SSH_PORT, sshPort)
            .value(RMI_PORT, rmiPort)
            .value(NAME, "Root Node");

    session.execute(insert);

    insert =
        QueryBuilder.insertInto(Constants.PATHSTORE_APPLICATIONS, Constants.DEPLOYMENT)
            .value(PATHSTORE_VERSION, QueryBuilder.now())
            .value(PATHSTORE_PARENT_TIMESTAMP, QueryBuilder.now())
            .value(PATHSTORE_DIRTY, true)
            .value(NEW_NODE_ID, 1)
            .value(PARENT_NODE_ID, -1)
            .value(PROCESS_STATUS, DeploymentProcessStatus.DEPLOYED.toString())
            .value(WAIT_FOR, new LinkedList<>(Collections.singleton(-1)))
            .value(SERVER_UUID, serverUUID.toString());

    session.execute(insert);

    cluster.close();
  }

  /** @return info message */
  @Override
  public String toString() {
    return "Writing server and deployment record to roots table";
  }
}
