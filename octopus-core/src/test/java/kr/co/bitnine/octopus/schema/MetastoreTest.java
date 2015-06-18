package kr.co.bitnine.octopus.schema;

import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.schema.model.MTable;
import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MetastoreTest {
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
    public void testMetastore() throws Exception
    {
        Configuration conf = new OctopusConfiguration();

        MetaStore metaStore = new MetaStore(conf);
        metaStore.addDataSource("sqlite", "org.sqlite.JDBC", SQLITE_URL, "sqlite database");

        MTable mtable = metaStore.getTable("bitnine");
        System.out.println("column cnt: " + mtable.getColumnCnt());
    }
}
