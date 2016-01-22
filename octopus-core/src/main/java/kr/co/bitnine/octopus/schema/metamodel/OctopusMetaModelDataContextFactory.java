/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.bitnine.octopus.schema.metamodel;

import com.datastax.driver.core.Cluster;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.simple.JSONObject;

public final class OctopusMetaModelDataContextFactory {

    private OctopusMetaModelDataContextFactory() {}

    public static DataContext createElasticSearchDataContext(JSONObject jsonObject) {
        DataContext dc;
        String host = (String) jsonObject.get("host");
        String port = (String) jsonObject.get("port");
        String database = (String) jsonObject.get("database");

        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress(host, Integer.parseInt(port)));
        dc = DataContextFactory.createElasticSearchDataContext(client, database);
        return dc;
    }

    public static DataContext createMongoDbDataContext(JSONObject jsonObject) {
        DataContext dc;
        String host = (String) jsonObject.get("host");
        String port = (String) jsonObject.get("port");
        String database = (String) jsonObject.get("database");
        String userName = (String) jsonObject.get("user");
        String password = (String) jsonObject.get("password");

        dc = DataContextFactory.createMongoDbDataContext(host, Integer.parseInt(port),
                database, userName, password.toCharArray());
        return dc;
    }

    public static DataContext createCouchDbDataContext(JSONObject jsonObject) {
        DataContext dc;
        String host = (String) jsonObject.get("host");
        String port = (String) jsonObject.get("port");
        String userName = (String) jsonObject.get("user");
        String password = (String) jsonObject.get("password");

        dc = DataContextFactory.createCouchDbDataContext(host, Integer.parseInt(port),
                userName, password);
        return dc;
    }

    public static DataContext createCassandraDataContext(JSONObject jsonObject) {
        DataContext dc;
        String host = (String) jsonObject.get("host");
        String port = (String) jsonObject.get("port");
        String database = (String) jsonObject.get("database");

        Cluster cluster = Cluster.builder().withPort(Integer.parseInt(port)).addContactPoint(host).build();
        dc = DataContextFactory.createCassandraDataContext(cluster, database);
        return dc;
    }
}
