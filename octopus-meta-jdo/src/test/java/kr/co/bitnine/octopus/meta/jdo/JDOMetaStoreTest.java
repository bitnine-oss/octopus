package kr.co.bitnine.octopus.meta.jdo;

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.model.MetaColumn;
import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.meta.model.MetaUser;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;
import kr.co.bitnine.octopus.testutils.MemoryDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

public class JDOMetaStoreTest
{
    private MemoryDatabase metaMemDb;
    private MemoryDatabase dataMemDb;
    private MetaStore metaStore;

    @Before
    public void setUp() throws Exception
    {
        metaMemDb = new MemoryDatabase("META");
        metaMemDb.start();

        dataMemDb = new MemoryDatabase("DATA");
        dataMemDb.start();
        dataMemDb.init();

        Properties conf = new Properties();
        conf.setProperty("metastore.jdo.connection.drivername", MemoryDatabase.DRIVER_NAME);
        conf.setProperty("metastore.jdo.connection.URL", metaMemDb.CONNECTION_STRING);
        conf.setProperty("metastore.jdo.connection.username", "");
        conf.setProperty("metastore.jdo.connection.password", "");

        metaStore = new JDOMetaStore();
        metaStore.start(conf);
    }

    @After
    public void tearDown() throws Exception
    {
        metaStore.stop();

        dataMemDb.stop();
        metaMemDb.stop();
    }

    @Test
    public void testAddJdbcDataSource() throws Exception
    {
        MetaContext mc = metaStore.getMetaContext();

        mc.addJdbcDataSource(MemoryDatabase.DRIVER_NAME, dataMemDb.CONNECTION_STRING, dataMemDb.NAME);

        MetaTable metaTable = mc.getTableByQualifiedName("DATA", "__DEFAULT", "BITNINE");
        Collection<MetaColumn> columns = metaTable.getColumns();
        System.out.println("number of columns: " + columns.size());
        for (MetaColumn metaColumn : columns)
            System.out.println("columnName=" + metaColumn.getName());

        Collection<MetaUser> users = mc.getUsers();
        for (MetaUser metaUser : users)
            System.out.println("UserName=" + metaUser.getName());

        mc.close();
    }

    @Test
    public void testSystemPrivilege() throws Exception
    {
        MetaContext mc = metaStore.getMetaContext();

        MetaUser user = mc.createUser("octopus", "bitnine");

        mc.addSystemPrivileges(
                Arrays.asList(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE, SystemPrivilege.GRANT_ANY_PRIVILEGE),
                Arrays.asList(user.getName()));

        mc.close();

        mc = metaStore.getMetaContext();

        user = mc.getUser("octopus");
        Set<SystemPrivilege> sysPrivs = user.getSystemPrivileges();
        for (SystemPrivilege sysPriv : sysPrivs)
            System.out.println(sysPriv.name());

        mc.removeSystemPrivileges(
                Arrays.asList(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE),
                Arrays.asList(user.getName()));

        mc.close();

        mc = metaStore.getMetaContext();

        user = mc.getUser("octopus");
        sysPrivs = user.getSystemPrivileges();
        for (SystemPrivilege sysPriv : sysPrivs)
            System.out.println(sysPriv.name());

        mc.close();
    }
}
