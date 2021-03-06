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

package pathstoreweb.pathstoreadminpanel.services.logs.payload;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import pathstore.client.PathStoreClientAuthenticatedCluster;
import pathstore.common.Constants;
import pathstore.system.logging.LoggerLevel;
import pathstoreweb.pathstoreadminpanel.validator.ValidatedPayload;

import java.util.Arrays;

import static pathstoreweb.pathstoreadminpanel.validator.ErrorConstants.GET_LOG_RECORDS_PAYLOAD.*;

/**
 * This payload is used to query a specific set of logs based on the node_id, the date and the
 * specific log level the user wants to see
 */
public final class GetLogRecordsPayload extends ValidatedPayload {

  /** Denotes the node_id requested */
  public final int nodeId;

  /** Denotes the date they want */
  public final String date;

  /** Denotes the specific log level they want */
  public final String logLevel;

  /**
   * @param node_id {@link #nodeId}
   * @param date {@link #date}
   * @param log_level {@link #logLevel}
   */
  public GetLogRecordsPayload(final int node_id, final String date, final String log_level) {
    this.nodeId = node_id;
    this.date = date;
    this.logLevel = log_level;
  }

  /**
   * TODO: Update when one is implemented
   *
   * <p>Validity check
   *
   * <p>(1): Valid form submission
   *
   * <p>(2): {@link #nodeId} is a valid node id
   *
   * <p>(3): {@link #date} the date is valid
   *
   * <p>(4): {@link #logLevel} valid log level
   *
   * @return true iff all above cases are held
   */
  @Override
  protected String[] calculateErrors() {

    // (1)
    if (this.bulkNullCheck(this.nodeId, this.date, this.logLevel))
      return new String[] {WRONG_SUBMISSION_FORMAT};

    String[] errors = {INVALID_NODE_ID, INVALID_DATE, null};

    Session session = PathStoreClientAuthenticatedCluster.getInstance().connect();

    // (2)
    Select deploymentSelect =
        QueryBuilder.select().from(Constants.PATHSTORE_APPLICATIONS, Constants.DEPLOYMENT);

    for (Row row : session.execute(deploymentSelect))
      if (row.getInt(Constants.DEPLOYMENT_COLUMNS.NEW_NODE_ID) == this.nodeId) errors[0] = null;

    // (3)
    Select availableDatesSelect =
        QueryBuilder.select().from(Constants.PATHSTORE_APPLICATIONS, Constants.AVAILABLE_LOG_DATES);
    availableDatesSelect
        .where(QueryBuilder.eq(Constants.AVAILABLE_LOG_DATES_COLUMNS.NODE_ID, this.nodeId))
        .and(QueryBuilder.eq(Constants.AVAILABLE_LOG_DATES_COLUMNS.DATE, this.date));

    for (Row row : session.execute(deploymentSelect)) errors[1] = null;

    // (4)
    if (Arrays.stream(LoggerLevel.values())
        .map(Enum::toString)
        .noneMatch(i -> i.equals(this.logLevel))) errors[2] = INVALID_LOG_LEVEL;

    return errors;
  }
}
