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

package pathstoreweb.pathstoreadminpanel.services.deployment.payload;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import pathstore.client.PathStoreClientAuthenticatedCluster;
import pathstore.common.Constants;
import pathstoreweb.pathstoreadminpanel.services.deployment.DeploymentRecord;
import pathstoreweb.pathstoreadminpanel.validator.ValidatedPayload;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pathstoreweb.pathstoreadminpanel.validator.ErrorConstants.ADD_DEPLOYMENT_RECORD_PAYLOAD.*;

/** Payload for the add deployment record endpoint. This is used to validate input */
public final class AddDeploymentRecordPayload extends ValidatedPayload {

  /** List of records to write */
  public List<DeploymentRecord> records;

  /**
   * Validity check
   *
   * <p>(1): Deployment record empty check
   *
   * <p>(2): Server UUID duplicates
   *
   * <p>(3): Node Id duplicates
   *
   * <p>(4): Unique server UUID's (not already used in a previous node deployment)
   *
   * <p>(5): unique node id's (not already used)
   *
   * <p>(6): parent id's valid
   *
   * <p>(7): no node has a node_id = parent_node_id
   *
   * <p>(8): valid server uuid (servers not already used)
   *
   * @return true iff all validity checks pass
   */
  @Override
  protected String[] calculateErrors() {

    // (1)
    if (this.records == null || this.records.size() == 0) return new String[] {EMPTY};

    String[] errors = {null, null, null, null, null, null, null};

    Set<String> serverUUIDSet =
        this.records.stream().map(i -> i.serverUUID).collect(Collectors.toSet());

    Set<Integer> nodeIdSet =
        this.records.stream().map(i -> i.newNodeId).collect(Collectors.toSet());

    // (2)
    if (serverUUIDSet.size() != this.records.size()) errors[0] = SERVER_UUID_DUPLICATES;

    // (3)
    if (nodeIdSet.size() != this.records.size()) errors[1] = NODE_ID_DUPLICATES;

    Session session = PathStoreClientAuthenticatedCluster.getInstance().connect();

    // (4) & (5) & (6)
    Select deploymentSelect =
        QueryBuilder.select().all().from(Constants.PATHSTORE_APPLICATIONS, Constants.DEPLOYMENT);

    Set<Integer> parentNodeIdSet =
        this.records.stream().map(i -> i.parentId).collect(Collectors.toSet());

    Set<Integer> alreadyInstalledNodeSet = new HashSet<>();

    for (Row row : session.execute(deploymentSelect)) {
      // (4)
      if (serverUUIDSet.contains(row.getString(Constants.DEPLOYMENT_COLUMNS.SERVER_UUID)))
        errors[2] = SERVER_UUID_IN_USE;

      int newNodeId = row.getInt(Constants.DEPLOYMENT_COLUMNS.NEW_NODE_ID);

      // (5)
      if (nodeIdSet.contains(newNodeId)) errors[3] = NODE_IDS_IN_USE;

      // (6) Setup
      alreadyInstalledNodeSet.add(newNodeId);
    }

    // (6)
    if (parentNodeIdSet.stream()
        .anyMatch(
            parentNodeId ->
                !(alreadyInstalledNodeSet.contains(parentNodeId)
                    || nodeIdSet.contains(parentNodeId)))) errors[4] = PARENT_ID_NOT_VALID;

    // (7)
    for (DeploymentRecord record : this.records)
      if (record.newNodeId == record.parentId) {
        errors[5] = NODE_ID_EQUALS_PARENT_ID;
        break;
      }

    // (8)
    Select serverSelect =
        QueryBuilder.select().all().from(Constants.PATHSTORE_APPLICATIONS, Constants.SERVERS);

    for (Row row : session.execute(serverSelect))
      serverUUIDSet.remove(row.getString(Constants.SERVERS_COLUMNS.SERVER_UUID));

    if (serverUUIDSet.size() > 0) errors[6] = SERVER_UUID_DOESNT_EXIST;

    return errors;
  }
}
