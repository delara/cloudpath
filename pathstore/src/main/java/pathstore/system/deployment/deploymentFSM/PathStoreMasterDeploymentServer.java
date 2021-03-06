/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package pathstore.system.deployment.deploymentFSM;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import pathstore.client.PathStoreSession;
import pathstore.common.Constants;
import pathstore.common.tables.DeploymentEntry;
import pathstore.common.tables.DeploymentProcessStatus;
import pathstore.system.PathStorePrivilegedCluster;
import pathstore.system.logging.PathStoreLogger;
import pathstore.system.logging.PathStoreLoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to read the deployment table and determine when to transition nodes.
 *
 * <p>Once a record has been transitions to deploying the slave deployment server will then execute
 * the deployment step. Once this step occurs it can either transition to deployed or failed. If
 * failed the administrator of the network will need to login to the web page in order to see the
 * error and request a retry, this retry rewrites the record of that node to deploying instead of
 * failed. This cycle could possibly continue until all errors are resolved. In order to avoid such
 * errors the administrator should follow the server setup guide on our github page.
 */
public class PathStoreMasterDeploymentServer implements Runnable {

  /** Logger */
  private final PathStoreLogger logger =
      PathStoreLoggerFactory.getLogger(PathStoreMasterDeploymentServer.class);

  /** Session used to interact with pathstore */
  private final PathStoreSession session =
      PathStorePrivilegedCluster.getDaemonInstance().psConnect();

  /**
   * This daemon will transition rows that are WAITING_DEPLOYMENT to DEPLOYING. The steps are:
   *
   * <p>(1): Query all records from the deployment table and store them into a set of node_id's
   * denoted as finished and a set of DeploymentEntry for the waiting records
   *
   * <p>(2): Iterate over all waiting deployment records and if the node they're waiting for has
   * finished transition that node
   *
   * <p>(3): Iterate over all waiting removal records and if the node they're waiting for has been
   * removed transition that node
   */
  @Override
  public void run() {
    try {
      while (true) {
        // (1)
        Select selectAllDeploymentRecords =
            QueryBuilder.select()
                .all()
                .from(Constants.PATHSTORE_APPLICATIONS, Constants.DEPLOYMENT);

        // Deployment
        Set<Integer> deployed = new HashSet<>();
        Set<DeploymentEntry> waitingDeployment = new HashSet<>();

        // Removal
        Set<DeploymentEntry> waitingRemoval = new HashSet<>();
        Set<Integer> completeSet = new HashSet<>();

        for (Row row : this.session.execute(selectAllDeploymentRecords)) {
          DeploymentEntry entry = DeploymentEntry.fromRow(row);

          // Setup filterable sets
          switch (entry.deploymentProcessStatus) {
            case DEPLOYED:
              deployed.add(entry.newNodeId);
              break;
            case WAITING_DEPLOYMENT:
              waitingDeployment.add(entry);
              break;
            case WAITING_REMOVAL:
              waitingRemoval.add(entry);
              break;
          }

          // add to complete set
          completeSet.add(entry.newNodeId);
        }

        // (2)
        waitingDeployment.stream()
            .filter(i -> deployed.containsAll(i.waitFor))
            .forEach(this::transitionDeploy);

        // (3) If all nodes i is waiting for aren't presented in the record set
        waitingRemoval.stream()
            .filter(i -> Collections.disjoint(i.waitFor, completeSet))
            .forEach(this::transitionRemoval);

        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          logger.error(e);
        }
      }
    } catch (Exception e) {
      logger.error(e);
    }
  }

  /**
   * Transition the node in the table from waiting to deploying
   *
   * @param entry entry to transition
   */
  private void transitionDeploy(final DeploymentEntry entry) {

    logger.info(
        String.format(
            "Deploying a new child to %d with id %d", entry.parentNodeId, entry.newNodeId));

    PathStoreDeploymentUtils.updateState(entry, DeploymentProcessStatus.DEPLOYING);
  }

  /**
   * Transition the node in the table from waiting to deploying
   *
   * @param entry entry to transition
   */
  private void transitionRemoval(final DeploymentEntry entry) {

    logger.info(
        String.format("%d is removing the child node %d", entry.parentNodeId, entry.newNodeId));

    PathStoreDeploymentUtils.updateState(entry, DeploymentProcessStatus.REMOVING);
  }
}
