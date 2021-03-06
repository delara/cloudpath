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

package pathstoreweb.pathstoreadminpanel.services.servers;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pathstore.client.PathStoreClientAuthenticatedCluster;
import pathstore.common.Constants;
import pathstore.common.tables.ServerAuthType;
import pathstore.common.tables.ServerIdentity;
import pathstoreweb.pathstoreadminpanel.services.IService;
import pathstoreweb.pathstoreadminpanel.services.servers.payload.UpdateServerPayload;

import java.io.IOException;

/** Simple service to update a server record */
public class UpdateServer implements IService {

  /** Data for the server record */
  private final UpdateServerPayload payload;

  /** @param payload {@link #payload} */
  public UpdateServer(final UpdateServerPayload payload) {
    this.payload = payload;
  }

  /**
   * Update server record and return 200 OK
   *
   * @return 200 OK
   */
  @Override
  public ResponseEntity<String> response() {

    try {
      this.updateServer();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new ResponseEntity<>(new JSONObject().toString(), HttpStatus.OK);
  }

  /** Set all rows with values from website as there is no need to make a read and then a select */
  private void updateServer() throws IOException {

    Session session = PathStoreClientAuthenticatedCluster.getInstance().connect();

    Update update = QueryBuilder.update(Constants.PATHSTORE_APPLICATIONS, Constants.SERVERS);

    Update.Assignments updateAssignment =
        update
            .where(
                QueryBuilder.eq(
                    Constants.SERVERS_COLUMNS.SERVER_UUID,
                    this.payload.server.serverUUID.toString()))
            .with(
                QueryBuilder.set(Constants.SERVERS_COLUMNS.USERNAME, this.payload.server.username))
            .and(QueryBuilder.set(Constants.SERVERS_COLUMNS.SSH_PORT, this.payload.server.sshPort))
            .and(QueryBuilder.set(Constants.SERVERS_COLUMNS.GRPC_PORT, this.payload.server.grpcPort))
            .and(QueryBuilder.set(Constants.SERVERS_COLUMNS.NAME, this.payload.server.name));

    // set proper rows for password auth type
    if (this.payload.server.authType.equals(ServerAuthType.PASSWORD.toString()))
      updateAssignment
          .and(
              QueryBuilder.set(
                  Constants.SERVERS_COLUMNS.AUTH_TYPE, ServerAuthType.PASSWORD.toString()))
          .and(QueryBuilder.set(Constants.SERVERS_COLUMNS.PASSWORD, this.payload.server.password));
    // set proper rows for keys type
    else if (this.payload.server.authType.equals(ServerAuthType.IDENTITY.toString()))
      updateAssignment
          .and(
              QueryBuilder.set(
                  Constants.SERVERS_COLUMNS.AUTH_TYPE, ServerAuthType.IDENTITY.toString()))
          .and(
              QueryBuilder.set(
                  Constants.SERVERS_COLUMNS.SERVER_IDENTITY,
                  new ServerIdentity(
                          this.payload.getPrivateKey().getBytes(), this.payload.server.passphrase)
                      .serialize()));
    else
      throw new RuntimeException(
          String.format("%s  is not a valid auth type", this.payload.server.authType));

    session.execute(update);
  }
}
