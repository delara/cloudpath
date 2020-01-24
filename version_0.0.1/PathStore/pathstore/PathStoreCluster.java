/**********
*
* Copyright 2019 Eyal de Lara, Seyed Hossein Mortazavi, Mohammad Salehe
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
***********/
package pathstore.client;

import pathstore.common.PathStoreProperties;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.AbstractSession;;

public class PathStoreCluster {
	
	// Create session to hosts  

	static PathStoreCluster pathStoreCluster=null;
	
	private Cluster cluster=null;
	
	PathStoreSession session=null;
	
	public static PathStoreCluster getInstance() {
		if (PathStoreCluster.pathStoreCluster == null) 
			PathStoreCluster.pathStoreCluster = new PathStoreCluster();
		return PathStoreCluster.pathStoreCluster;
	}
	
	
	public PathStoreCluster() {
//		System.out.println("Cluster IP " + 
//				PathStoreProperties.getInstance().CassandraIP + " Port " + 
//				PathStoreProperties.getInstance().CassandraPort);
		this.cluster = new Cluster.Builder()
				.addContactPoints(PathStoreProperties.getInstance().CassandraIP)
				.withPort(PathStoreProperties.getInstance().CassandraPort)
	            .withSocketOptions(new SocketOptions().setTcpNoDelay(true).setReadTimeoutMillis(15000000))
	            .withQueryOptions(
	                    new QueryOptions()
	                        .setRefreshNodeIntervalMillis(0)
	                        .setRefreshNodeListIntervalMillis(0)
	                        .setRefreshSchemaIntervalMillis(0)
	                )
	            .build();
		session = new PathStoreSession(this.cluster);

	}
	
	
	public PathStoreCluster(PathStoreProperties custom) {
//		System.out.println("Cluster IP " + 
//				custom.CassandraIP + " Port " + 
//				custom.CassandraPort);
		this.cluster = new Cluster.Builder()
				.addContactPoints(custom.CassandraIP)
				.withPort(custom.CassandraPort)
	            .withSocketOptions(new SocketOptions().setTcpNoDelay(true).setReadTimeoutMillis(15000000))
	            .withQueryOptions(
	                    new QueryOptions()
	                        .setRefreshNodeIntervalMillis(0)
	                        .setRefreshNodeListIntervalMillis(0)
	                        .setRefreshSchemaIntervalMillis(0)
	                )
	            .build();
		
		session = new PathStoreSession(this.cluster);
	}
	
	public  Metadata getMetadata()
	{
		return cluster.getMetadata();
	}
	
	public int getClusterId()
	{
		return PathStoreProperties.getInstance().NodeID;
	}

	
	public PathStoreSession connect() {
		return this.session;
	}

}
