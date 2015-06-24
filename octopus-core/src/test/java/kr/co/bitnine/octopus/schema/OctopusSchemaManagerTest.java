package kr.co.bitnine.octopus.schema;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.Assert.*;

public class OctopusSchemaManagerTest {
    @Test
    public void test() throws Exception
    {
        OctopusSchemaManager mgr = new OctopusSchemaManager("kisung");
    }

    private static final String SQLITE_URL = "jdbc:sqlite:file::memory:?cache=shared";

    private Connection initialConnection;

    @Before
    public void setUp() throws Exception
    {
        Class.forName("org.sqlite.JDBC");

        initialConnection = DriverManager.getConnection(SQLITE_URL);

        Statement stmt = initialConnection.createStatement();
        stmt.executeUpdate("CREATE TABLE bitnine (id INTEGER, name STRING)");
        stmt.executeUpdate("INSERT INTO bitnine VALUES(9, 'jsyang')");
    }

    @After
    public void tearDown() throws Exception
    {
        initialConnection.close();
    }

    @Test
    public void testSqlite3() throws Exception
    {
        Connection conn = DriverManager.getConnection(SQLITE_URL);

        /*
        Database db = new Database(conn);
        OctopusSchema[] schemas = db.getSchemas();

        conn.close();

        for (OctopusSchema schema : schemas) {
            System.out.println("schema: " + schema.getName());

            for (String name : schema.getTableNames())
                System.out.println("table: " + name);
        }

        org.apache.calcite.schema.Schema s = db.getSchema();
        org.apache.calcite.schema.Schema ss = s.getSubSchema("__DEFAULT");
        for (String name : ss.getTableNames())
            System.out.println("table: " + name);
            */
    }
}