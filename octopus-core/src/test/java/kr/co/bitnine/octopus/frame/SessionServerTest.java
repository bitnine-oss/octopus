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

package kr.co.bitnine.octopus.frame;

import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.MetaStoreService;
import kr.co.bitnine.octopus.meta.MetaStores;
import kr.co.bitnine.octopus.schema.SchemaManager;
import kr.co.bitnine.octopus.testutils.MemoryDatabase;
import kr.co.bitnine.octopus.util.NetUtils;
import org.apache.hadoop.conf.Configuration;
import org.junit.*;

import java.net.InetSocketAddress;
import java.sql.*;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

public class SessionServerTest
{
    private static MemoryDatabase metaMemDb;
    private static MemoryDatabase dataMemDb;
    private static MetaStoreService metaStoreService;
    private static SchemaManager schemaManager;
    private static SessionServer sessionServer;

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        Class.forName("kr.co.bitnine.octopus.Driver");

        metaMemDb = new MemoryDatabase("META");
        metaMemDb.start();

        dataMemDb = new MemoryDatabase("DATA");
        dataMemDb.start();
        dataMemDb.init();

        Configuration conf = new OctopusConfiguration();
        conf.set("metastore.jdo.connection.drivername", MemoryDatabase.DRIVER_NAME);
        conf.set("metastore.jdo.connection.URL", metaMemDb.CONNECTION_STRING);
        conf.set("metastore.jdo.connection.username", "");
        conf.set("metastore.jdo.connection.password", "");

        MetaStore metaStore = MetaStores.newInstance(conf.get("metastore.class"));
        metaStoreService = new MetaStoreService(metaStore);
        metaStoreService.init(conf);
        metaStoreService.start();

        schemaManager = new SchemaManager(metaStore);
        schemaManager.init(conf);
        schemaManager.start();

        sessionServer = new SessionServer(metaStore, schemaManager);
        sessionServer.init(conf);
        sessionServer.start();

        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("ALTER SYSTEM ADD DATASOURCE " + dataMemDb.NAME + " CONNECT BY '" + dataMemDb.CONNECTION_STRING + "'");
        stmt.close();
        conn.close();
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
        sessionServer.stop();
        schemaManager.stop();
        metaStoreService.stop();

        dataMemDb.stop();
        metaMemDb.stop();
    }

    private static Connection getConnection(String user, String password) throws Exception
    {
        InetSocketAddress addr = NetUtils.createSocketAddr("127.0.0.1:58000");
        String url = "jdbc:octopus://" + NetUtils.getHostPortString(addr) + "/db";

        Properties info = new Properties();
        info.setProperty("user", user);
        info.setProperty("password", password);

        return DriverManager.getConnection(url, info);
    }

    @Test
    public void testSelect() throws Exception
    {
        Connection conn = getConnection("octopus", "bitnine");

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ID, NAME FROM BITNINE;");
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println("id=" + id + ", name=" + name);
        }
        rs.close();
        stmt.close();

/*
        PreparedStatement pstmt = conn.prepareStatement("SELECT ID, NAME FROM BITNINE WHERE NAME = ?");
        pstmt.setString(1, "octopus");
        rs = pstmt.executeQuery();
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println("id=" + id + ", name=" + name);
        }
        rs.close();
        pstmt.close();
 */

        conn.close();
    }

    @Test
    public void testUser() throws Exception
    {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE USER jsyang IDENTIFIED BY '0009';");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        assertFalse(conn.isClosed());
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("ALTER USER jsyang IDENTIFIED BY 'jsyang' REPLACE '0009';");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "jsyang");
        assertFalse(conn.isClosed());
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("DROP USER jsyang;");
        stmt.close();
        conn.close();
    }

    @Test
    public void testRole() throws Exception
    {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE ROLE rnd;");
        stmt.execute("DROP ROLE rnd;");

        stmt.close();
        conn.close();
    }

    @Test
    public void testGrantRevokeSysPrivs() throws Exception
    {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE USER jsyang IDENTIFIED BY '0009';");

        stmt.execute("GRANT ALL PRIVILEGES TO jsyang;");

        String query = "REVOKE ALTER SYSTEM, " +
                "CREATE USER, ALTER USER, DROP USER, " +
                "COMMENT ANY, " +
                "GRANT ANY OBJECT PRIVILEGE, GRANT ANY PRIVILEGE " +
                "FROM jsyang;";
        stmt.execute(query);

        stmt.execute("REVOKE ALL PRIVILEGES FROM jsyang;");

        stmt.execute("DROP USER jsyang;");

        stmt.close();
        conn.close();
    }

    @Test
    public void testSystemPrivileges() throws Exception
    {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE USER jsyang IDENTIFIED BY '0009'");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        stmt = conn.createStatement();
        try {
            stmt.execute("ALTER SYSTEM ADD DATASOURCE " + dataMemDb.NAME + " CONNECT BY '" + dataMemDb.CONNECTION_STRING + "'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        } finally {
            stmt.close();
            conn.close();
        }

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("DROP USER jsyang;");
        stmt.close();
        conn.close();
    }
}
