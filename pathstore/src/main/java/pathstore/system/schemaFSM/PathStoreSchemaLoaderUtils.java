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
package pathstore.system.schemaFSM;

import com.datastax.driver.core.Session;

import java.util.Arrays;
import java.util.stream.Stream;

/** This is a utility class for the schema loader */
public class PathStoreSchemaLoaderUtils {

  /**
   * This is a hardcoded function that allows for loading the base application schema.
   *
   * <p>Its keyspace name is pathstore_applications.
   *
   * @param session database session to execute on
   */
  public static void loadApplicationSchema(final Session session) {
    String schema =
        "CREATE KEYSPACE pathstore_applications WITH REPLICATION = { 'class' : 'org.apache.cassandra.locator.SimpleStrategy', 'replication_factor': '1' } AND DURABLE_WRITES = false;\n"
            + "CREATE TABLE pathstore_applications.view_available_log_dates (\n"
            + "    node_id int,\n"
            + "    date text,\n"
            + "    pathstore_view_id uuid,\n"
            + "    pathstore_version timeuuid,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (pathstore_view_id, node_id, date, pathstore_version)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.available_log_dates (\n"
            + "    node_id int,\n"
            + "    date text,\n"
            + "    pathstore_version timeuuid,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (node_id, date, pathstore_version)\n"
            + ") WITH CLUSTERING ORDER BY (date DESC, pathstore_version DESC)\n"
            + "   AND read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.0\n"
            + "   AND gc_grace_seconds = 604800\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = 'table definitions'\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "CREATE INDEX available_log_dates_pathstore_deleted_idx ON pathstore_applications.available_log_dates (pathstore_deleted);\n"
            + "CREATE INDEX available_log_dates_pathstore_dirty_idx ON pathstore_applications.available_log_dates (pathstore_dirty);\n"
            + "CREATE INDEX available_log_dates_pathstore_parent_timestamp_idx ON pathstore_applications.available_log_dates (pathstore_parent_timestamp);\n"
            + "CREATE INDEX available_log_dates_pathstore_node_idx ON pathstore_applications.available_log_dates (pathstore_node);\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.view_logs (\n"
            + "    node_id int,\n"
            + "    date text,\n"
            + "    log_level text,\n"
            + "    count int,\n"
            + "    log text,\n"
            + "    pathstore_view_id uuid,\n"
            + "    pathstore_version timeuuid,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (pathstore_view_id, node_id, date, log_level, count, pathstore_version)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.logs (\n"
            + "    node_id int,\n"
            + "    date text,\n"
            + "    log_level text,\n"
            + "    count int,\n"
            + "    log text,\n"
            + "    pathstore_version timeuuid,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY ((node_id, date, log_level), count, pathstore_version)\n"
            + ") WITH CLUSTERING ORDER BY (count DESC, pathstore_version DESC)\n"
            + "   AND read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.0\n"
            + "   AND gc_grace_seconds = 604800\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = 'table definitions'\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "CREATE INDEX logs_pathstore_deleted_idx ON pathstore_applications.logs (pathstore_deleted);\n"
            + "CREATE INDEX logs_pathstore_dirty_idx ON pathstore_applications.logs (pathstore_dirty);\n"
            + "CREATE INDEX logs_pathstore_parent_timestamp_idx ON pathstore_applications.logs (pathstore_parent_timestamp);\n"
            + "CREATE INDEX logs_pathstore_node_idx ON pathstore_applications.logs (pathstore_node);\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.view_deployment (\n"
            + "    new_node_id int,\n"
            + "    parent_node_id int,\n"
            + "    process_status text,\n"
            + "    wait_for list<int>,\n"
            + "    server_uuid text,\n"
            + "    pathstore_view_id uuid,\n"
            + "    pathstore_version timeuuid,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (pathstore_view_id, parent_node_id, new_node_id, pathstore_version)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.deployment (\n"
            + "    new_node_id int,\n"
            + "    parent_node_id int,\n"
            + "    process_status text,\n"
            + "    wait_for list<int>,\n"
            + "    server_uuid text,\n"
            + "    pathstore_version timeuuid,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (parent_node_id, new_node_id, pathstore_version)\n"
            + ") WITH CLUSTERING ORDER BY (new_node_id DESC, pathstore_version DESC)\n"
            + "   AND read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.0\n"
            + "   AND gc_grace_seconds = 604800\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = 'table definitions'\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "CREATE INDEX deployment_pathstore_deleted_idx ON pathstore_applications.deployment (pathstore_deleted);\n"
            + "CREATE INDEX deployment_pathstore_dirty_idx ON pathstore_applications.deployment (pathstore_dirty);\n"
            + "CREATE INDEX deployment_pathstore_parent_timestamp_idx ON pathstore_applications.deployment (pathstore_parent_timestamp);\n"
            + "CREATE INDEX deployment_pathstore_node_idx ON pathstore_applications.deployment (pathstore_node);\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.view_servers (\n"
            + "    server_uuid text,\n"
            + "    ip text,\n"
            + "    username text,\n"
            + "    auth_type text,\n"
            + "    password text,\n"
            + "    ssh_port int,\n"
            + "    grpc_port int,\n"
            + "    name text,\n"
            + "    pathstore_view_id uuid,\n"
            + "    pathstore_version timeuuid,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (pathstore_view_id, server_uuid, pathstore_version)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.servers (\n"
            + "    server_uuid text,\n"
            + "    ip text,\n"
            + "    username text,\n"
            + "    auth_type text,\n"
            + "    password text,\n"
            + "    server_identity blob,\n"
            + "    ssh_port int,\n"
            + "    grpc_port int,\n"
            + "    name text,\n"
            + "    pathstore_version timeuuid,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (server_uuid, pathstore_version)\n"
            + ") WITH CLUSTERING ORDER BY (pathstore_version DESC)\n"
            + "   AND read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.0\n"
            + "   AND gc_grace_seconds = 604800\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = 'table definitions'\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "CREATE INDEX servers_pathstore_deleted_idx ON pathstore_applications.servers (pathstore_deleted);\n"
            + "CREATE INDEX servers_pathstore_dirty_idx ON pathstore_applications.servers (pathstore_dirty);\n"
            + "CREATE INDEX servers_pathstore_parent_timestamp_idx ON pathstore_applications.servers (pathstore_parent_timestamp);\n"
            + "CREATE INDEX servers_pathstore_node_idx ON pathstore_applications.servers (pathstore_node);\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.view_node_schemas (\n"
            + "    pathstore_view_id uuid,\n"
            + "    node_id int,\n"
            + "    pathstore_version timeuuid,\n"
            + "    keyspace_name text,\n"
            + "    process_status text,\n"
            + "    wait_for list<int>,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (pathstore_view_id, node_id, keyspace_name, pathstore_version)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.node_schemas (\n"
            + "    node_id int,\n"
            + "    pathstore_version timeuuid,\n"
            + "    keyspace_name text,\n"
            + "    process_status text,\n"
            + "    wait_for list<int>,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (node_id, keyspace_name, pathstore_version)\n"
            + ") WITH CLUSTERING ORDER BY (keyspace_name DESC, pathstore_version DESC)\n"
            + "   AND read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.0\n"
            + "   AND gc_grace_seconds = 604800\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = 'table definitions'\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "CREATE INDEX node_schemas_pathstore_deleted_idx ON pathstore_applications.node_schemas (pathstore_deleted);\n"
            + "CREATE INDEX node_schemas_pathstore_dirty_idx ON pathstore_applications.node_schemas (pathstore_dirty);\n"
            + "CREATE INDEX node_schemas_pathstore_parent_timestamp_idx ON pathstore_applications.node_schemas (pathstore_parent_timestamp);\n"
            + "CREATE INDEX node_schemas_pathstore_node_idx ON pathstore_applications.node_schemas (pathstore_node);\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.view_application_credentials (\n"
            + "    pathstore_view_id uuid,\n"
            + "    pathstore_version timeuuid,\n"
            + "    keyspace_name text,\n"
            + "    password text,\n"
            + "    is_super_user boolean,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (pathstore_view_id, keyspace_name, pathstore_version)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.application_credentials (\n"
            + "    pathstore_version timeuuid,\n"
            + "    keyspace_name text,\n"
            + "    password text,\n"
            + "    is_super_user boolean,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (keyspace_name, pathstore_version)\n"
            + ") WITH CLUSTERING ORDER BY (pathstore_version DESC)\n"
            + "   AND read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.0\n"
            + "   AND gc_grace_seconds = 604800\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = 'table definitions'\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "CREATE INDEX application_credentials_pathstore_deleted_idx ON pathstore_applications.application_credentials (pathstore_deleted);\n"
            + "CREATE INDEX application_credentials_pathstore_dirty_idx ON pathstore_applications.application_credentials (pathstore_dirty);\n"
            + "CREATE INDEX application_credentials_pathstore_parent_timestamp_idx ON pathstore_applications.application_credentials (pathstore_parent_timestamp);\n"
            + "CREATE INDEX application_credentials_pathstore_node_idx ON pathstore_applications.application_credentials (pathstore_node);"
            + "\n"
            + "CREATE TABLE pathstore_applications.view_application_lease_time (\n"
            + "    pathstore_view_id uuid,\n"
            + "    pathstore_version timeuuid,\n"
            + "    keyspace_name text,\n"
            + "    client_lease_time int,\n"
            + "    server_additional_time int,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (pathstore_view_id, keyspace_name, pathstore_version)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.application_lease_time (\n"
            + "    pathstore_version timeuuid,\n"
            + "    keyspace_name text,\n"
            + "    client_lease_time int,\n"
            + "    server_additional_time int,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (keyspace_name, pathstore_version)\n"
            + ") WITH CLUSTERING ORDER BY (pathstore_version DESC)\n"
            + "   AND read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.0\n"
            + "   AND gc_grace_seconds = 604800\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = 'table definitions'\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "CREATE INDEX application_lease_time_pathstore_deleted_idx ON pathstore_applications.application_lease_time (pathstore_deleted);\n"
            + "CREATE INDEX application_lease_time_pathstore_dirty_idx ON pathstore_applications.application_lease_time (pathstore_dirty);\n"
            + "CREATE INDEX application_lease_time_pathstore_parent_timestamp_idx ON pathstore_applications.application_lease_time (pathstore_parent_timestamp);\n"
            + "CREATE INDEX application_lease_time_pathstore_node_idx ON pathstore_applications.application_lease_time (pathstore_node);"
            + "\n"
            + "CREATE TABLE pathstore_applications.view_apps (\n"
            + "    pathstore_view_id uuid,\n"
            + "    pathstore_version timeuuid,\n"
            + "    augmented_schema text,\n"
            + "    keyspace_name text,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (pathstore_view_id, keyspace_name, pathstore_version)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "\n"
            + "CREATE TABLE pathstore_applications.apps (\n"
            + "    pathstore_version timeuuid,\n"
            + "    augmented_schema text,\n"
            + "    keyspace_name text,\n"
            + "    pathstore_deleted boolean,\n"
            + "    pathstore_dirty boolean,\n"
            + "    pathstore_node int,\n"
            + "    pathstore_parent_timestamp timeuuid,\n"
            + "    PRIMARY KEY (keyspace_name, pathstore_version)\n"
            + ") WITH CLUSTERING ORDER BY (pathstore_version DESC)\n"
            + "   AND read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.0\n"
            + "   AND gc_grace_seconds = 604800\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = 'table definitions'\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;\n"
            + "CREATE INDEX apps_pathstore_deleted_idx ON pathstore_applications.apps (pathstore_deleted);\n"
            + "CREATE INDEX apps_pathstore_dirty_idx ON pathstore_applications.apps (pathstore_dirty);\n"
            + "CREATE INDEX apps_pathstore_parent_timestamp_idx ON pathstore_applications.apps (pathstore_parent_timestamp);\n"
            + "CREATE INDEX apps_pathstore_node_idx ON pathstore_applications.apps (pathstore_node);"
            + "\n"
            + "CREATE TABLE pathstore_applications.local_startup (\n"
            + "    task_done int,\n"
            + "    PRIMARY KEY (task_done)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;"
            + "\n"
            + "CREATE TABLE pathstore_applications.local_node_auth (\n"
            + "    node_id int,\n"
            + "    username text,\n"
            + "    password text,\n"
            + "    PRIMARY KEY (node_id)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;"
            + "\n"
            + "CREATE TABLE pathstore_applications.local_client_auth (\n"
            + "    keyspace_name text,\n"
            + "    username text,\n"
            + "    password text,\n"
            + "    is_super_user boolean,\n"
            + "    PRIMARY KEY (keyspace_name)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;"
            + "\n"
            + "CREATE TABLE pathstore_applications.local_auxiliary_auth (\n"
            + "    name text,\n"
            + "    username text,\n"
            + "    password text,\n"
            + "    PRIMARY KEY (name)\n"
            + ") WITH read_repair_chance = 0.0\n"
            + "   AND dclocal_read_repair_chance = 0.1\n"
            + "   AND gc_grace_seconds = 864000\n"
            + "   AND bloom_filter_fp_chance = 0.01\n"
            + "   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }\n"
            + "   AND comment = ''\n"
            + "   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold' : 32, 'min_threshold' : 4 }\n"
            + "   AND compression = { 'chunk_length_in_kb' : 64, 'class' : 'org.apache.cassandra.io.compress.LZ4Compressor' }\n"
            + "   AND default_time_to_live = 0\n"
            + "   AND speculative_retry = '99PERCENTILE'\n"
            + "   AND min_index_interval = 128\n"
            + "   AND max_index_interval = 2048\n"
            + "   AND crc_check_chance = 1.0;";

    parseSchema(schema).forEach(session::execute);
  }

  /**
   * Simple function to filter out commands when a schema is passed
   *
   * @param schema schema to parse
   * @return stream of commands from passed schema to avoid collecting them to a list
   */
  public static Stream<String> parseSchema(final String schema) {
    return Arrays.stream(schema.split(";")).map(String::trim).filter(i -> i.length() > 0);
  }
}
