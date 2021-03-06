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

package pathstoreweb.pathstoreadminpanel.validator;

/**
 * This class is used to denote all error constants that could occur during the execution of an API
 * endpoint
 */
public final class ErrorConstants {

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.applicationmanagement.payload.AddApplicationDeploymentRecordPayload}
   */
  public static final class ADD_APPLICATION_DEPLOYMENT_RECORD_PAYLOAD {
    public static final String EMPTY = "You must pass at least one record";
    public static final String TO_MANY_KEYSPACES =
        "You can only pass one keyspace update per request";
    public static final String INVALID_RECORD =
        "Each record must reference a valid node id and it must not already have the given application installed on the node";
    public static final String INVALID_WAIT_FOR =
        "Each record must wait for their parent node unless the node is the root node which it must wait for -1";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.applicationmanagement.payload.DeleteApplicationDeploymentRecordPayload}
   */
  public static final class DELETE_APPLICATION_DEPLOYMENT_RECORD_PAYLOAD {
    public static final String EMPTY = ADD_APPLICATION_DEPLOYMENT_RECORD_PAYLOAD.EMPTY;
    public static final String TO_MANY_KEYSPACES =
        ADD_APPLICATION_DEPLOYMENT_RECORD_PAYLOAD.TO_MANY_KEYSPACES;
    public static final String INVALID_RECORD =
        "Each record must reference a valid node id and it must already have the given keyspace installed on it";
    public static final String INVALID_WAIT_FOR =
        "Each record must wait for all their children unless they have no children it must be -1";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.applications.payload.AddApplicationPayload}
   */
  public static final class ADD_APPLICATION_PAYLOAD {
    public static final String WRONG_SUBMISSION_FORMAT =
        "You must submit the fields: application_name which is the desired name for your application and application_schema which is a valid cql file";
    public static final String IMPROPER_APPLICATION_NAME_FORM =
        "Your application name must start with pathstore_";
    public static final String APPLICATION_NAME_NOT_UNIQUE =
        "The application name you passed is already used";
    public static final String CLIENT_LEASE_TIME_OUT_OF_BOUNDS =
        "Client lease time must be greater than 0";
    public static final String SERVER_ADDITIONAL_TIME_OUT_OF_BOUNDS =
        "Server additional time must be greater than 0";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.applications.payload.RemoveApplicationPayload}
   */
  public static final class REMOVE_APPLICATION_PAYLOAD {
    public static final String WRONG_SUBMISSION_FORMAT = "You must pass a valid application name";
    public static final String APPLICATION_DOESNT_EXIST =
        "The application name passed does not exist";
    public static final String APPLICATION_IS_DEPLOYED =
        "In order to delete an application from the network it must not be deployed on any node";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.servers.payload.AddServerPayload}
   */
  public static final class ADD_SERVER_PAYLOAD {
    public static final String WRONG_SUBMISSION_FORMAT =
        "You must submit the fields: ip, username, ssh_port, grpc_port, name";
    public static final String IP_IS_NOT_UNIQUE =
        "You must use an ip address that isn't already in use";
    public static final String NAME_IS_NOT_UNIQUE = "You must use a name that isn't already in use";
    public static final String CONNECTION_INFORMATION_IS_INVALID =
        "The connection information you provided is invalid";
    public static final String PASSWORD_NOT_PRESENT =
        "You must submit a password field when using password authentication";
    public static final String PRIVATE_KEY_NOT_PRESENT =
        "You must submit a private key file when using key based authentication";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.servers.payload.UpdateServerPayload}
   */
  public static final class UPDATE_SERVER_PAYLOAD {
    public static final String WRONG_SUBMISSION_FORMAT =
        "You must submit the following fields: server_uuid, ip, username, ssh_port, grpc_port, name";
    public static final String SERVER_UUID_DOESNT_EXIST =
        "The server uuid you passed does not exist";
    public static final String IP_IS_NOT_UNIQUE =
        "The ip you've submitted conflicts with another ip in the record set that is not the original ip given";
    public static final String NAME_IS_NOT_UNIQUE =
        "The name you've submitted conflicts with another server name in the record set that is not the original name given";
    public static final String SERVER_UUID_IS_NOT_FREE =
        "You cannot modify a server record that is attached to an existing pathstore node";
    public static final String CONNECTION_INFORMATION_IS_INVALID =
        "The connection information you provided is invalid";
    public static final String PASSWORD_NOT_PRESENT =
        "You must submit a password field when using password authentication";
    public static final String PRIVATE_KEY_NOT_PRESENT =
        "You must submit a private key file when using key based authentication";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.servers.payload.DeleteServerPayload}
   */
  public static final class DELETE_SERVER_PAYLOAD {
    public static final String WRONG_SUBMISSION_FORMAT =
        "You must submit the following fields: server_uuid";
    public static final String SERVER_UUID_DOESNT_EXIST = "The server uuid does not exist";
    public static final String SERVER_UUID_IS_NOT_FREE =
        "The server uuid passed cannot be attached to a pathstore node";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.deployment.payload.AddDeploymentRecordPayload}
   */
  public static final class ADD_DEPLOYMENT_RECORD_PAYLOAD {
    public static final String EMPTY = "You cannot pass an empty list of deployment objects";
    public static final String SERVER_UUID_DUPLICATES = "You cannot have duplicate server uuid's";
    public static final String NODE_ID_DUPLICATES = "You cannot have duplicate node id's";
    public static final String SERVER_UUID_IN_USE = "A server uuid passed is already in use";
    public static final String NODE_IDS_IN_USE = "A node id passed is already in use";
    public static final String PARENT_ID_NOT_VALID =
        "A parent id passed does not point to a valid node";
    public static final String NODE_ID_EQUALS_PARENT_ID =
        "You cannot have a node where the node id equals the parent id";
    public static final String SERVER_UUID_DOESNT_EXIST = "A server uuid passed does not exist";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.deployment.payload.UpdateDeploymentRecordPayload}
   */
  public static final class UPDATE_DEPLOYMENT_RECORD_PAYLOAD {
    public static final String INVALID_FAILED_ENTRY = "You must enter a valid failed entry";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.deployment.payload.DeleteDeploymentRecordPayload}
   */
  public static final class DELETE_DEPLOYMENT_RECORD_PAYLOAD {
    public static final String EMPTY = ADD_DEPLOYMENT_RECORD_PAYLOAD.EMPTY;
    public static final String INVALID_RECORD = "You must only pass records that are DEPLOYED";
  }

  /**
   * Validity errors for {@link
   * pathstoreweb.pathstoreadminpanel.services.logs.payload.GetLogRecordsPayload}
   */
  public static final class GET_LOG_RECORDS_PAYLOAD {
    public static final String WRONG_SUBMISSION_FORMAT =
        "You must submit the following fields: node_id, date, log_level";
    public static final String INVALID_NODE_ID = "The node id passed is invalid";
    public static final String INVALID_DATE = "The date passed does not have any records";
    public static final String INVALID_LOG_LEVEL = "The log level passed is invalid";
  }
}
