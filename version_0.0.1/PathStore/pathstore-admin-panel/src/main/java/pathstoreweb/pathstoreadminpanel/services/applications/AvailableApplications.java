package pathstoreweb.pathstoreadminpanel.services.applications;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import pathstore.client.PathStoreCluster;
import pathstore.common.Constants;
import pathstoreweb.pathstoreadminpanel.services.applications.formatter.AvailableApplicationsFormatter;
import pathstoreweb.pathstoreadminpanel.services.IService;

import java.util.LinkedList;
import java.util.List;

/**
 * Gathers a list of all available applications that can be installed on the pathstore network
 *
 * @see AvailableApplicationsFormatter
 */
public class AvailableApplications implements IService {

  /** @return formats data from {@link #getApplications()} */
  @Override
  public String response() {
    return new AvailableApplicationsFormatter(this.getApplications()).format();
  }

  /**
   * Selects all apps from the APPS table, parses them into a list of {@link Application}
   *
   * @return list of available applications
   */
  private List<Application> getApplications() {
    Session session = PathStoreCluster.getInstance().connect();

    Select queryApplications =
        QueryBuilder.select().all().from(Constants.PATHSTORE_APPLICATIONS, Constants.APPS);

    LinkedList<Application> applications = new LinkedList<>();

    // TODO: Figure out if this calls pathstoreiterator or not. If not use an actual for each loop
    session
        .execute(queryApplications)
        .forEach(
            i ->
                applications.addFirst(
                    new Application(
                        i.getString(Constants.APPS_COLUMNS.KEYSPACE_NAME),
                        i.getString(Constants.APPS_COLUMNS.AUGMENTED_SCHEMA))));
    return applications;
  }
}
