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

import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.frame.ConnectionManager;
import kr.co.bitnine.octopus.frame.SessionFactory;
import kr.co.bitnine.octopus.frame.SessionFactoryImpl;
import kr.co.bitnine.octopus.frame.SessionServer;
import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.MetaStoreService;
import kr.co.bitnine.octopus.meta.MetaStores;
import kr.co.bitnine.octopus.meta.logs.StdoutUpdateLoggerFactory;
import kr.co.bitnine.octopus.meta.model.MetaUser;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;
import kr.co.bitnine.octopus.schema.SchemaManager;
import kr.co.bitnine.octopus.testutils.MemoryDatabase;
import kr.co.bitnine.octopus.util.NetUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OctopusMetaModelTest {
    private static final Log LOG = LogFactory.getLog(SchemaManager.class);

    private static String type = "elasticsearch";
    private static String host = "localhost";
    private static String port = "19102";
    private static String databaseName = "twitter";
    private static String tableName = "peopletype";

    private static String datasourceName = "motamodeltest";
    private static String connectionString = "{" +
            "\"type\": \"" + type + "\", " +
            "\"host\": \"" + host + "\", " +
            "\"port\": \"" + port + "\", " +
            "\"database\": \"" + databaseName + "\"" +
            "}";
    private static String driverName = "metamodel";

    private static MemoryDatabase metaMemDb;
    private static MetaStoreService metaStoreService;
    private static ConnectionManager connectionManager;
    private static SchemaManager schemaManager;

    private static SessionServer sessionServer;
    private static EmbeddedElasticsearchServer embeddedElasticsearchServer;
    private static Client client;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            LOG.info("start JUnit test: " + description.getDisplayName());
        }

        @Override
        protected void finished(Description description) {
            LOG.info("finished. JUnit test: " + description.getDisplayName());
        }
    };

    @BeforeClass
    public static void setUpClass() throws Exception {
        Class.forName("kr.co.bitnine.octopus.Driver");
    }

    @Before
    public void setUp() throws Exception {
        metaMemDb = new MemoryDatabase("meta");
        metaMemDb.start();

        embeddedElasticsearchServer = new EmbeddedElasticsearchServer();
        client = embeddedElasticsearchServer.getClient();
        client.prepareIndex(databaseName, tableName).setId("99").setSource(buildPeopleJson("LEE", 35)).execute().actionGet();

        // The refresh API allows to explicitly refresh one or more index,
        // making all operations performed since the last refresh available for
        // search
        embeddedElasticsearchServer.getClient().admin().indices().prepareRefresh().execute().actionGet();
        System.out.println("Embedded ElasticSearch server created!");

        Configuration conf = new OctopusConfiguration();
        conf.set("metastore.jdo.connection.drivername", MemoryDatabase.DRIVER_NAME);
        conf.set("metastore.jdo.connection.URL", metaMemDb.connectionString);
        conf.set("metastore.jdo.connection.username", "");
        conf.set("metastore.jdo.connection.password", "");

        MetaStore metaStore = MetaStores.newInstance(conf.get("metastore.class"));
        metaStoreService = new MetaStoreService(metaStore,
                new StdoutUpdateLoggerFactory());
        metaStoreService.init(conf);
        metaStoreService.start();

        MetaContext metaContext = metaStore.getMetaContext();
        MetaUser user = metaContext.createUser("octopus", "bitnine");
        metaContext.addSystemPrivileges(Arrays.asList(SystemPrivilege.values()), Arrays.asList(user.getName()));

        connectionManager = new ConnectionManager(metaStore);
        connectionManager.init(conf);
        connectionManager.start();

        schemaManager = SchemaManager.getSingletonInstance(metaStore);
        schemaManager.init(conf);
        schemaManager.start();

        SessionFactory sessFactory = new SessionFactoryImpl(
                metaStore, connectionManager, schemaManager);
        sessionServer = new SessionServer(sessFactory);
        sessionServer.init(conf);
        sessionServer.start();


        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("ALTER SYSTEM ADD DATASOURCE \"" + datasourceName
                + "\" CONNECT TO '" + connectionString
                + "' USING '" + driverName + "'");
        stmt.close();
        conn.close();
    }

    @After
    public void tearDown() throws Exception {
        sessionServer.stop();
        schemaManager.stop();
        connectionManager.stop();
        metaStoreService.stop();

        embeddedElasticsearchServer.shutdown();
        metaMemDb.stop();
        System.out.println("Server shut down!");
    }

    private static Connection getConnection(String user, String password) throws Exception {
        InetSocketAddress addr = NetUtils.createSocketAddr("127.0.0.1:58000");
        String url = "jdbc:octopus://" + NetUtils.getHostPortString(addr);

        Properties info = new Properties();
        info.setProperty("user", user);
        info.setProperty("password", password);

//        info.setProperty("prepareThreshold", "-1");
        info.setProperty("prepareThreshold", "1");

//        info.setProperty("binaryTransfer", "true");

        return DriverManager.getConnection(url, info);
    }

    @Test
    public void testAddDataSourceExists() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        exception.expect(SQLException.class);
        stmt.execute("ALTER SYSTEM ADD DATASOURCE \"" + datasourceName
                + "\" CONNECT TO '" + connectionString
                + "' USING '" + driverName + "'");

        stmt.close();
        conn.close();
    }

    @Test
    public void testSelect() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT _id, age, name FROM motamodeltest.twitter.peopletype");

        if (rs.next()) {
            assertEquals("99", rs.getString(1));
            assertEquals("35", rs.getString(2));
            assertEquals("LEE", rs.getString(3));
        }

        rs = stmt.executeQuery("SELECT age, _id, name FROM motamodeltest.twitter.peopletype");

        if (rs.next()) {
            assertEquals("35", rs.getString(1));
        }

        rs.close();
        stmt.close();
        conn.close();
    }

    private static XContentBuilder buildPeopleJson(String name, int age) throws IOException {
        return jsonBuilder().startObject().field("name", name).field("age", age).endObject();
    }
}
