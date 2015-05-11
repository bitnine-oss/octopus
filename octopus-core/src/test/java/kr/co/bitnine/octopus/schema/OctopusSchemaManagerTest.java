package kr.co.bitnine.octopus.schema;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class OctopusSchemaManagerTest
{
    private static final String SQLITE_URL = "jdbc:sqlite:file::memory:?cache=shared";
    private Connection initialConnection;

    @Before
    public void setUp() throws Exception
    {
        Class.forName("org.sqlite.JDBC");

        initialConnection = DriverManager.getConnection(SQLITE_URL);

        Statement stmt = initialConnection.createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS bitnine");
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

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM bitnine");
        while (rs.next())
            System.out.println("id: " + rs.getInt("id") + ", name: " + rs.getString("name"));

        conn.close();
    }
}