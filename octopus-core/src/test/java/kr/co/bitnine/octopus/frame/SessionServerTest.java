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
import org.apache.hadoop.conf.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SessionServerTest {
    private static MemoryDatabase metaMemDb;
    private static MemoryDatabase dataMemDb;
    private static MetaStoreService metaStoreService;
    private static ConnectionManager connectionManager;
    private static SchemaManager schemaManager;
    private static SessionServer sessionServer;

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @BeforeClass
    public static void setUpClass() throws Exception {
        Class.forName("kr.co.bitnine.octopus.Driver");
        metaMemDb = new MemoryDatabase("meta");
        metaMemDb.start();

        dataMemDb = new MemoryDatabase("data");
        dataMemDb.start();
        dataMemDb.importJSON(SessionServerTest.class.getClass(), "/sample.json");

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

        schemaManager = new SchemaManager(metaStore);
        schemaManager.init(conf);
        schemaManager.start();

        SessionFactory sessFactory = new SessionFactoryImpl(
                metaStore, connectionManager, schemaManager);
        sessionServer = new SessionServer(sessFactory);
        sessionServer.init(conf);
        sessionServer.start();

        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("ALTER SYSTEM ADD DATASOURCE \"" + dataMemDb.name
                + "\" CONNECT TO '" + dataMemDb.connectionString
                + "' USING '" + MemoryDatabase.DRIVER_NAME + "'");
        stmt.close();
        conn.close();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        sessionServer.stop();
        schemaManager.stop();
        metaStoreService.stop();

        dataMemDb.stop();
        metaMemDb.stop();
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
        stmt.execute("ALTER SYSTEM ADD DATASOURCE \"" + dataMemDb.name
                + "\" CONNECT TO '" + dataMemDb.connectionString
                + "' USING '" + MemoryDatabase.DRIVER_NAME + "'");

        stmt.close();
        conn.close();
    }

    private boolean existDataSource(DatabaseMetaData metaData, String name) throws SQLException {
        ResultSet rs = metaData.getCatalogs();
        while (rs.next()) {
            String dsName = rs.getString("TABLE_CAT");
            System.out.println(" *** " + dsName);
            if (dsName.equals(name))
                return true;
        }
        return false;
    }

    private boolean existTable(DatabaseMetaData metaData, String dsName, String name) throws SQLException {
        ResultSet rs = metaData.getTables(dsName, "%DEFAULT", "%", null);
        while (rs.next()) {
            String tblName = rs.getString("TABLE_NAME");
            System.out.println(" *** " + tblName);
            if (tblName.equals(name))
                return true;
        }
        return false;
    }

    private int checkNumRows(Statement stmt, String tblName) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM \"" + tblName + '"');
        if (!rs.next())
            return 0;
        int numRows = rs.getInt(1);
        rs.close();
        return numRows;
    }

    @Test
    public void testDropDataSource1() throws Exception {
        /* add a new dataSource and populate some data */
        MemoryDatabase newMemDb = new MemoryDatabase("DATA2");
        newMemDb.start();

        final String tblName = "TMP";
        newMemDb.runExecuteUpdate("CREATE TABLE \"" + tblName + "\" (ID INTEGER, NAME STRING)");

        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("ALTER SYSTEM ADD DATASOURCE \"" + newMemDb.name
                + "\" CONNECT TO '" + newMemDb.connectionString
                + "' USING '" + MemoryDatabase.DRIVER_NAME + "'");

        DatabaseMetaData metaData = conn.getMetaData();

        assertTrue(existDataSource(metaData, newMemDb.name));
        assertTrue(existTable(metaData, newMemDb.name, tblName));

        stmt.execute("ALTER SYSTEM DROP DATASOURCE \"" + newMemDb.name + '"');

        assertFalse(existDataSource(metaData, newMemDb.name));
        assertFalse(existTable(metaData, newMemDb.name, tblName));

        /* cleanup */
        stmt.close();
        conn.close();
        newMemDb.stop();
        schemaManager.resetDataSourcePool();
    }

    @Test
    public void testDropDataSource2() throws Exception {
        MemoryDatabase newMemDb = new MemoryDatabase("DATA2");
        newMemDb.start();

        newMemDb.runExecuteUpdate("CREATE TABLE \"TMP\" (\"ID\" INTEGER, \"NAME\" STRING)");

        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("ALTER SYSTEM ADD DATASOURCE \"" + newMemDb.name
                + "\" CONNECT TO '" + newMemDb.connectionString
                + "' USING '" + MemoryDatabase.DRIVER_NAME + "'");

        stmt.execute("CREATE USER \"yjchoi\" IDENTIFIED BY 'piggy'");
        stmt.execute("GRANT SELECT ON \"" + newMemDb.name + "\".\"__DEFAULT\" TO \"yjchoi\"");

        ResultSet rs = stmt.executeQuery("SHOW OBJECT PRIVILEGES FOR \"yjchoi\"");
        int numRows = 0;
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("PRIVILEGE"));
            ++numRows;
        }
        rs.close();
        assertEquals(numRows, 1);

        stmt.execute("ALTER SYSTEM DROP DATASOURCE \"" + newMemDb.name + '"');

        rs = stmt.executeQuery("SHOW OBJECT PRIVILEGES FOR \"yjchoi\"");
        numRows = 0;
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("PRIVILEGE"));
            ++numRows;
        }
        rs.close();
        assertEquals(numRows, 0);

        stmt.execute("DROP USER \"yjchoi\"");

        stmt.close();
        conn.close();
        newMemDb.stop();
        schemaManager.resetDataSourcePool();
    }

    @Test
    public void testUpdateDataSource1() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        final String tblName = "TMP";
        dataMemDb.runExecuteUpdate("CREATE TABLE \"" + tblName + "\" (\"ID\" INTEGER, \"NAME\" STRING)");
        dataMemDb.runExecuteUpdate("INSERT INTO \"" + tblName + "\" VALUES (1, 'yjchoi')");

        boolean exceptionCaught = false;
        try {
            checkNumRows(stmt, tblName);
        } catch (SQLException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);

        int rows = checkNumRows(stmt, "employee");
        assertEquals(rows, 10);

        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getTables(dataMemDb.name, "%DEFAULT", "%", null);
        while (rs.next())
            System.out.println(" *** " + rs.getString("TABLE_NAME"));

        stmt.execute("ALTER SYSTEM UPDATE DATASOURCE \"" + dataMemDb.name + '"');

        metaData = conn.getMetaData();
        rs = metaData.getTables(dataMemDb.name, "%DEFAULT", "%", null);
        while (rs.next())
            System.out.println(" *** " + rs.getString("TABLE_NAME"));

        rows = checkNumRows(stmt, tblName);
        assertEquals(rows, 1);

        stmt.close();
        conn.close();
    }

    @Test
    public void testUpdateDataSource2() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE USER \"yjchoi\" IDENTIFIED BY 'piggy'");
        stmt.execute("GRANT SELECT ON \"" + dataMemDb.name + "\".\"__DEFAULT\" TO \"yjchoi\"");

        Connection conn2 = getConnection("yjchoi", "piggy");
        Statement stmt2 = conn.createStatement();

        int rows = checkNumRows(stmt2, "employee");
        assertEquals(rows, 10);

        ResultSet rs;
        DatabaseMetaData metaData = conn.getMetaData();
        System.out.println("* Columns");
        rs = metaData.getColumns(dataMemDb.name, "%DEFAULT", "employee", "%");
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("TABLE_NAME") + ", "
                    + rs.getString("COLUMN_NAME") + ", "
                    + rs.getString("REMARKS"));
        }
        rs.close();

        stmt.execute("ALTER SYSTEM UPDATE DATASOURCE \"" + dataMemDb.name + '"');

        metaData = conn.getMetaData();
        System.out.println("* Columns");
        rs = metaData.getColumns(dataMemDb.name, "%DEFAULT", "employee", "%");
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("TABLE_NAME") + ", "
                    + rs.getString("COLUMN_NAME") + ", "
                    + rs.getString("REMARKS"));
        }
        rs.close();

        /* privileges should be preserved after update dataSource */
        rows = checkNumRows(stmt2, "employee");
        assertEquals(rows, 10);

        stmt2.close();
        conn2.close();
        stmt.close();
        conn.close();
    }

    @Test
    public void testUpdateDataSource3() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        final String comment = "commentOnTable";
        final String tblName = "employee";

        stmt.execute("COMMENT ON TABLE \"" + dataMemDb.name + "\".\"__DEFAULT\".\"" + tblName + "\" IS '" + comment + "'");
        DatabaseMetaData metaData = conn.getMetaData();

        ResultSet rs = metaData.getTables(dataMemDb.name, "%DEFAULT", tblName, null);
        while (rs.next()) {
            if (rs.getString("TABLE_NAME").equals(tblName))
                assertTrue(rs.getString("REMARKS").equals(comment));
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("TABLE_NAME") + ", "
                    + rs.getString("REMARKS"));
        }
        rs.close();

        stmt.execute("ALTER SYSTEM UPDATE DATASOURCE \"" + dataMemDb.name + '"');

        rs = metaData.getTables(dataMemDb.name, "%DEFAULT", tblName, null);
        while (rs.next()) {
            if (rs.getString("TABLE_NAME").equals(tblName))
                assertTrue(rs.getString("REMARKS").equals(comment));
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("TABLE_NAME") + ", "
                    + rs.getString("REMARKS"));
        }
        rs.close();

        stmt.close();
        conn.close();
    }

    @Test
    public void testUpdateDataSource4() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        dataMemDb.runExecuteUpdate("CREATE TABLE AA1 (ID INTEGER, NAME STRING)");
        dataMemDb.runExecuteUpdate("CREATE TABLE AA2 (ID INTEGER, NAME STRING)");
        dataMemDb.runExecuteUpdate("CREATE TABLE BB1 (ID INTEGER, NAME STRING)");
        dataMemDb.runExecuteUpdate("INSERT INTO AA1 VALUES (1, 'yjchoi')");

        boolean exceptionCaught = false;
        try {
            checkNumRows(stmt, "AA1");
        } catch (SQLException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);

        stmt.execute("ALTER SYSTEM UPDATE TABLE \"" + dataMemDb.name + "\".\"__DEFAULT\".'AA%'");

        int rows = checkNumRows(stmt, "AA1");
        assertEquals(1, rows);

        rows = checkNumRows(stmt, "AA2");
        assertEquals(0, rows);

        exceptionCaught = false;
        try {
            checkNumRows(stmt, "BB1");
        } catch (SQLException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);

        dataMemDb.runExecuteUpdate("DROP TABLE AA1");
        dataMemDb.runExecuteUpdate("DROP TABLE AA2");
        dataMemDb.runExecuteUpdate("DROP TABLE BB1");

        stmt.close();
        conn.close();
    }

    @Test
    public void testSelect() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");

        Statement stmt = conn.createStatement();
        try {
            stmt.executeQuery("SELECT ID, NAME FROM BIT9");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT \"id\", \"name\" FROM \"employee\"");
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println("id=" + id + ", name=" + name);
        }
        rs.close();
        stmt.close();

//        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement("SELECT \"id\", \"name\" FROM \"employee\" WHERE \"id\" >= ?");
        pstmt.setMaxRows(3);
//        pstmt.setFetchSize(3);
        pstmt.setInt(1, 7);
        for (int i = 0; i < 2; i++) {
            rs = pstmt.executeQuery();
            rs.next();
            rs.close();
        }
        rs = pstmt.executeQuery();
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println("id=" + id + ", name=" + name);
        }
        rs.close();
        pstmt.close();

        conn.close();
    }

    @Test
    public void testComplexSelect() throws Exception {
        MemoryDatabase newMemDb = new MemoryDatabase("DATA2");
        newMemDb.start();

        newMemDb.runExecuteUpdate("CREATE TABLE \"TMP\" (\"ID\" INTEGER, \"NAME\" STRING)");

        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("ALTER SYSTEM ADD DATASOURCE \"" + newMemDb.name
                + "\" CONNECT TO '" + newMemDb.connectionString
                + "' USING '" + MemoryDatabase.DRIVER_NAME + "'");

        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT \"EM\".\"id\", \"EM\".\"name\" " +
                        "FROM \"employee\" \"EM\", " +
                        "\"DATA2\".\"__DEFAULT\".\"TMP\" \"TM\" " +
                        "WHERE \"EM\".\"id\" = \"TM\".\"ID\"");
        rs.close();
        stmt.close();
        conn.close();
        newMemDb.stop();
        schemaManager.resetDataSourcePool();
    }

    @Test
    public void testUser() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE USER \"jsyang\" IDENTIFIED BY '0009'");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        assertFalse(conn.isClosed());
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("ALTER USER \"jsyang\" IDENTIFIED BY 'jsyang' REPLACE '0009'");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "jsyang");
        assertFalse(conn.isClosed());
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("DROP USER \"jsyang\"");
        stmt.close();
        conn.close();
    }

    @Test
    public void testRole() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        stmt.execute("CREATE ROLE rnd");
        stmt.execute("DROP ROLE rnd");

        stmt.close();
        conn.close();
    }

    @Test
    public void testSystemPrivileges() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE USER \"jsyang\" IDENTIFIED BY '0009'");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        stmt = conn.createStatement();

        try {
            stmt.execute("ALTER SYSTEM ADD DATASOURCE \"" + dataMemDb.name
                    + "\" CONNECT TO '" + dataMemDb.connectionString
                    + "' USING '" + MemoryDatabase.DRIVER_NAME + "'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("CREATE USER \"kskim\" IDENTIFIED BY 'vp'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("ALTER USER \"jsyang\" IDENTIFIED BY 'jsyang' REPLACE '0009'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("DROP USER \"jsyang\"");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("GRANT CREATE USER TO \"jsyang\"");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("REVOKE CREATE USER FROM \"octopus\"");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("COMMENT ON TABLE \"" + dataMemDb.name + "\".\"__DEFAULT\".\"employee\" IS 'test'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("SET DATACATEGORY ON COLUMN \"" + dataMemDb.name + "\".\"__DEFAULT\".\"employee\".\"name\" IS 'category'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        stmt.close();
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();

        stmt.execute("GRANT ALL PRIVILEGES TO \"jsyang\"");
        String query = "REVOKE ALTER SYSTEM, "
                + "SELECT ANY TABLE, "
                + "ALTER USER, DROP USER, "
                + "COMMENT ANY, "
                + "GRANT ANY OBJECT PRIVILEGE, GRANT ANY PRIVILEGE "
                + "FROM \"jsyang\"";
        stmt.execute(query);

        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        stmt = conn.createStatement();

        stmt.execute("CREATE USER \"kskim\" IDENTIFIED BY 'vp'");

        try {
            stmt.execute("DROP USER \"kskim\"");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        stmt.close();
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("REVOKE CREATE USER FROM \"jsyang\"");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        stmt = conn.createStatement();
        try {
            stmt.execute("CREATE USER \"bitnine\" IDENTIFIED BY 'password'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }
        stmt.close();
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("DROP USER \"kskim\"");
        stmt.execute("DROP USER \"jsyang\"");
        stmt.close();
        conn.close();
    }

    @Test
    public void testSelectPrivilege() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE USER \"jsyang\" IDENTIFIED BY '0009'");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        stmt = conn.createStatement();
        try {
            stmt.executeQuery("SELECT \"id\", \"name\" FROM \"employee\";");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("GRANT SELECT ON \"" + dataMemDb.name + "\".\"__DEFAULT\" TO \"jsyang\"");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        stmt = conn.createStatement();
        stmt.executeQuery("SELECT \"id\", \"name\" FROM \"employee\"").close();
        stmt.close();
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("DROP USER \"jsyang\"");
        stmt.close();
        conn.close();
    }

    @Test
    public void testShow() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");

        System.out.println("* Transaction isolation level");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SHOW TRANSACTION ISOLATION LEVEL");
        while (rs.next())
            System.out.println("  " + rs.getString("transaction_isolation"));
        rs.close();

        System.out.println("* DataSources");
        DatabaseMetaData metaData = conn.getMetaData();
        rs = metaData.getCatalogs();
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("REMARKS"));
        }
        rs.close();

        System.out.println("* Schemas");
        rs = metaData.getSchemas(dataMemDb.name, "%DEFAULT");
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("TABLE_CATALOG") + ", "
                    + rs.getString("REMARKS") + ", "
                    + rs.getString("TABLE_CAT_REMARKS"));
        }
        rs.close();

        System.out.println("* Tables");
        rs = metaData.getTables(dataMemDb.name, "%DEFAULT", "employee", null);
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("TABLE_NAME") + ", "
                    + rs.getString("REMARKS") + ", "
                    + rs.getString("TABLE_CAT_REMARKS") + ", "
                    + rs.getString("TABLE_SCHEM_REMARKS"));
        }
        rs.close();

        System.out.println("* Columns");
        rs = metaData.getColumns(dataMemDb.name, "%DEFAULT", "employee", "%");
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("TABLE_NAME") + ", "
                    + rs.getString("COLUMN_NAME") + ", "
                    + rs.getString("REMARKS") + ", "
                    + rs.getString("TABLE_CAT_REMARKS") + ", "
                    + rs.getString("TABLE_SCHEM_REMARKS") + ", "
                    + rs.getString("TABLE_NAME_REMARKS"));
        }
        rs.close();

        System.out.println("* Users");

        rs = stmt.executeQuery("SHOW ALL USERS");
        while (rs.next()) {
            System.out.println("  " + rs.getString("USER_NAME") + ", "
                    + rs.getString("REMARKS"));
        }
        rs.close();

        stmt.execute("CREATE USER \"jsyang\" IDENTIFIED BY '0009'");
        stmt.execute("GRANT ALL ON \"" + dataMemDb.name + "\".\"__DEFAULT\" TO \"jsyang\"");
        rs = stmt.executeQuery("SHOW OBJECT PRIVILEGES FOR \"jsyang\"");
        while (rs.next()) {
            System.out.println("  " + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("PRIVILEGE"));
        }
        rs.close();
        stmt.execute("DROP USER \"jsyang\"");

        stmt.close();

        conn.close();
    }

    @Test
    public void testShowComments() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");

        System.out.println("* Comments");
        Statement stmt = conn.createStatement();

        stmt.execute("COMMENT ON DATASOURCE \"" + dataMemDb.name + "\" IS 'DS_COMMENT'");
        stmt.execute("COMMENT ON SCHEMA \"" + dataMemDb.name + "\".\"__DEFAULT\" IS 'SCHEMA_COMMENT'");
        stmt.execute("COMMENT ON TABLE \"" + dataMemDb.name + "\".\"__DEFAULT\".\"employee\" IS 'TABLE_COMMENT'");
        stmt.execute("COMMENT ON COLUMN \"" + dataMemDb.name + "\".\"__DEFAULT\".\"employee\".\"name\" IS 'COLUMN_COMMENT_EXTRA'");

        ResultSet rs = stmt.executeQuery("SHOW COMMENTS '%COMMENT' TABLE 'emp%' ");
        int rowCnt = 0;
        while (rs.next()) {
            ++rowCnt;
            System.out.println("  " + rs.getString("OBJECT_TYPE") + ", "
                    + rs.getString("TABLE_CAT") + ", "
                    + rs.getString("TABLE_SCHEM") + ", "
                    + rs.getString("TABLE_NAME") + ", "
                    + rs.getString("COLUMN_NAME") + ", "
                    + rs.getString("TABLE_CAT_REMARKS") + ", "
                    + rs.getString("TABLE_SCHEM_REMARKS") + ", "
                    + rs.getString("TABLE_NAME_REMARKS") + ", "
                    + rs.getString("COLUMN_NAME_REMARKS"));
        }
        rs.close();
        assertEquals(rowCnt, 3);

        stmt.close();
        conn.close();
    }

    @Test
    public void testComment() throws Exception {
        Connection conn = getConnection("octopus", "bitnine");
        Statement stmt = conn.createStatement();

        stmt.execute("COMMENT ON DATASOURCE \"" + dataMemDb.name + "\" IS 'dataSource'");
        stmt.execute("COMMENT ON SCHEMA \"" + dataMemDb.name + "\".\"__DEFAULT\" IS 'schema'");
        stmt.execute("COMMENT ON TABLE \"" + dataMemDb.name + "\".\"__DEFAULT\".\"employee\" IS 'table'");
        stmt.execute("COMMENT ON COLUMN \"" + dataMemDb.name + "\".\"__DEFAULT\".\"employee\".\"name\" IS 'column'");
        stmt.execute("COMMENT ON USER \"octopus\" IS 'superuser'");

        stmt.execute("CREATE USER \"jsyang\" IDENTIFIED BY '0009';");

        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        stmt = conn.createStatement();

        try {
            stmt.execute("COMMENT ON DATASOURCE \"" + dataMemDb.name + "\" IS 'dataSource'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("COMMENT ON USER \"octopus\" IS 'superuser'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        try {
            stmt.execute("COMMENT ON SCHEMA \"" + dataMemDb.name + "\".\"__DEFAULT\" IS 'schema'");
        } catch (SQLException e) {
            System.out.println("expected exception - " + e.getMessage());
        }

        stmt.close();
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("GRANT COMMENT ON \"" + dataMemDb.name + "\".\"__DEFAULT\" TO \"jsyang\"");
        stmt.close();
        conn.close();

        conn = getConnection("jsyang", "0009");
        stmt = conn.createStatement();
        stmt.execute("COMMENT ON SCHEMA \"" + dataMemDb.name + "\".\"__DEFAULT\" IS 'schema'");
        stmt.execute("COMMENT ON TABLE \"" + dataMemDb.name + "\".\"__DEFAULT\".\"employee\" IS 'table'");
        stmt.execute("COMMENT ON COLUMN \"" + dataMemDb.name + "\".\"__DEFAULT\".\"employee\".\"name\" IS 'column'");
        stmt.close();
        conn.close();

        conn = getConnection("octopus", "bitnine");
        stmt = conn.createStatement();
        stmt.execute("DROP USER \"jsyang\"");
        stmt.close();
        conn.close();
    }
}
