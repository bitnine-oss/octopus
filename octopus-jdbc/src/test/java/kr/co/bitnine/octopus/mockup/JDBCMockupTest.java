package kr.co.bitnine.octopus.mockup;

import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

public class JDBCMockupTest {
    @Before
    public void setUp() throws Exception
    {
        Class.forName("kr.co.bitnine.octopus.mockup.Driver");
    }

    @Test
    public void testStartup() throws Exception {
        String url = "jdbc:octopus-mockup://localhost/db";
        Properties info = new Properties();
        info.setProperty("user", "octopus");
        info.setProperty("password", "bitnine");
        Connection conn = DriverManager.getConnection(url, info);

        DatabaseMetaData dbmd = conn.getMetaData();
        /* 1. getting Datasource list */
        /* 2. getting Schema list */
        /* 3. getting Table list */
        ResultSet tables = dbmd.getTables(null, null, null, null);
        while (tables.next()) {
            System.out.println("Table name: " + tables.getString(3));
        }
        /* 4. getting Column list */
        ResultSet columns = dbmd.getColumns(null, null, null, null);
        while (columns.next()) {
            System.out.println("Column name: " + columns.getString(4));
        }
    }
}
