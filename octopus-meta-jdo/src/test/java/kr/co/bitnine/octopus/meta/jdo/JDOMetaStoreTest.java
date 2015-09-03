package kr.co.bitnine.octopus.meta.jdo;

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.model.MetaColumn;
import kr.co.bitnine.octopus.meta.model.MetaSchemaPrivilege;
import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.meta.model.MetaUser;
import kr.co.bitnine.octopus.meta.privilege.ObjectPrivilege;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;
import kr.co.bitnine.octopus.testutils.MemoryDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JDOMetaStoreTest
{
    private static final String DATASOURCE_NAME = "DATA";
    private static final String SCHEMA_NAME = "__DEFAULT";
    private static final String TABLE_NAME = "BITNINE";

    private MemoryDatabase metaMemDb;
    private MemoryDatabase dataMemDb;
    private MetaStore metaStore;

    @Before
    public void setUp() throws Exception
    {
        metaMemDb = new MemoryDatabase("META");
        metaMemDb.start();

        dataMemDb = new MemoryDatabase(DATASOURCE_NAME);
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

        mc.getDataSource(DATASOURCE_NAME);
        mc.getSchemaByQualifiedName(DATASOURCE_NAME, SCHEMA_NAME);
        mc.getColumnByQualifiedName(DATASOURCE_NAME, SCHEMA_NAME, TABLE_NAME, "NAME");

        MetaTable metaTable = mc.getTableByQualifiedName(DATASOURCE_NAME, SCHEMA_NAME, TABLE_NAME);
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
        for (SystemPrivilege sysPriv : user.getSystemPrivileges())
            System.out.println(sysPriv.name());
        mc.removeSystemPrivileges(
                Arrays.asList(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE),
                Arrays.asList(user.getName()));
        mc.close();

        mc = metaStore.getMetaContext();
        user = mc.getUser("octopus");
        for (SystemPrivilege sysPriv : user.getSystemPrivileges())
            System.out.println(sysPriv.name());
        mc.close();
    }

    @Test
    public void testObjectPrivilege() throws Exception
    {
        final String[] schemaName = new String[] {DATASOURCE_NAME, SCHEMA_NAME};

        MetaContext mc = metaStore.getMetaContext();
        mc.addJdbcDataSource(MemoryDatabase.DRIVER_NAME, dataMemDb.CONNECTION_STRING, dataMemDb.NAME);
        mc.close();

        mc = metaStore.getMetaContext();
        MetaSchemaPrivilege schemaPriv = mc.getSchemaPrivileges(schemaName, "octopus");
        assertNull(schemaPriv);
        mc.close();

        mc = metaStore.getMetaContext();
        MetaUser user = mc.createUser("octopus", "bitnine");
        mc.addObjectPrivileges(
                Arrays.asList(ObjectPrivilege.values()),
                schemaName,
                Arrays.asList(user.getName()));
        mc.close();

        mc = metaStore.getMetaContext();
        schemaPriv = mc.getSchemaPrivileges(schemaName, "octopus");
        assertNotNull(schemaPriv);
        for (ObjectPrivilege objPriv : schemaPriv.getObjectPrivileges())
            System.out.println(objPriv.name());
        mc.close();

        mc = metaStore.getMetaContext();
        mc.removeObjectPrivileges(
                Arrays.asList(ObjectPrivilege.COMMENT),
                schemaName,
                Arrays.asList(user.getName()));
        mc.close();

        mc = metaStore.getMetaContext();
        schemaPriv = mc.getSchemaPrivileges(schemaName, "octopus");
        assertNotNull(schemaPriv);
        for (ObjectPrivilege objPriv : schemaPriv.getObjectPrivileges())
            System.out.println(objPriv.name());
        mc.close();

        mc = metaStore.getMetaContext();
        mc.removeObjectPrivileges(
                Arrays.asList(ObjectPrivilege.SELECT),
                schemaName,
                Arrays.asList(user.getName()));
        mc.close();

        mc = metaStore.getMetaContext();
        schemaPriv = mc.getSchemaPrivileges(schemaName, "octopus");
        assertNull(schemaPriv);
        mc.close();
    }
}
