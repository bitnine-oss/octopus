package kr.co.bitnine.octopus;

import org.apache.hadoop.conf.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TestDb
{
    private static final String DRIVER_NAME = "org.sqlite.JDBC";
    private static final String METASTORE_URL = "jdbc:sqlite:file:metastore?mode=memory&cache=shared";
    private static final String TESTDB_URL = "jdbc:sqlite:file:testdb?mode=memory&cache=shared";

    private Connection metastoreConnection;
    private Connection initialConnection;

    public TestDb() throws Exception
    {
        Class.forName(DRIVER_NAME);
    }

    public void create() throws Exception
    {
        metastoreConnection = DriverManager.getConnection(METASTORE_URL);
        initialConnection = DriverManager.getConnection(TESTDB_URL);

        Statement stmt = initialConnection.createStatement();
        stmt.executeUpdate("CREATE TABLE BITNINE (ID INTEGER, NAME STRING)");
        stmt.executeUpdate("INSERT INTO BITNINE VALUES(9, 'jsyang')");
    }

    public void setMetaStoreConf(Configuration conf)
    {
        conf.set("metastore.jdo.connection.URL", METASTORE_URL);
        conf.set("metastore.jdo.connection.drivername", DRIVER_NAME);
        conf.set("metastore.jdo.connection.username", "");
        conf.set("metastore.jdo.connection.password", "");
    }

    public Connection getMetaStoreConnection() throws Exception
    {
        return DriverManager.getConnection(METASTORE_URL);
    }

    public Connection getTestDbConnection() throws Exception
    {
        return DriverManager.getConnection(TESTDB_URL);
    }

    public Connection getInitialConnection()
    {
        return initialConnection;
    }

    public void destroy() throws Exception
    {
        initialConnection.close();
        metastoreConnection.close();
    }

    public String getDriverName()
    {
        return DRIVER_NAME;
    }

    public String getMetaStoreURL()
    {
        return METASTORE_URL;
    }

    public String getTestDbURL()
    {
        return TESTDB_URL;
    }
}
