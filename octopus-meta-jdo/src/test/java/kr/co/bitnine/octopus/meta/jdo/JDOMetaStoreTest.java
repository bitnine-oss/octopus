package kr.co.bitnine.octopus.meta.jdo;

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.model.MetaColumn;
import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.testutils.MemoryDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Properties;

public class JDOMetaStoreTest
{
    private MemoryDatabase metaMemDb;
    private MemoryDatabase dataMemDb;

    @Before
    public void setUp() throws Exception
    {
        metaMemDb = new MemoryDatabase("META");
        metaMemDb.start();

        dataMemDb = new MemoryDatabase("DATA");
        dataMemDb.start();
        dataMemDb.init();
    }

    @After
    public void tearDown() throws Exception
    {
        dataMemDb.stop();
        metaMemDb.stop();
    }

    @Test
    public void test() throws Exception
    {
        Properties conf = new Properties();
        conf.setProperty("metastore.jdo.connection.drivername", MemoryDatabase.DRIVER_NAME);
        conf.setProperty("metastore.jdo.connection.URL", metaMemDb.CONNECTION_STRING);
        conf.setProperty("metastore.jdo.connection.username", "");
        conf.setProperty("metastore.jdo.connection.password", "");

        MetaStore metaStore = new JDOMetaStore();
        metaStore.start(conf);

        MetaContext mc = metaStore.getMetaContext();

        mc.addJdbcDataSource(MemoryDatabase.DRIVER_NAME, dataMemDb.CONNECTION_STRING, dataMemDb.NAME);

        MetaTable metaTable = mc.getTableByName("BITNINE");
        Collection<MetaColumn> columns = metaTable.getColumns();
        System.out.println("number of columns: " + columns.size());
        for (MetaColumn metaColumn : columns)
            System.out.println("columnName=" + metaColumn.getName());

        mc.close();

        metaStore.stop();
    }
}
