package kr.co.bitnine.octopus.meta.jdo;

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.model.*;
import kr.co.bitnine.octopus.meta.privilege.ObjectPrivilege;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;
import kr.co.bitnine.octopus.testutils.MemoryDatabase;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.*;

public class JDOMetaContextTest
{
    private static final String SCHEMA_NAME = "__DEFAULT";

    private static MemoryDatabase memDbData;

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        memDbData = new MemoryDatabase("data");
        memDbData.start();
        memDbData.importJSON(JDOMetaContextTest.class.getClass(), "/sample.json");
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
        memDbData.stop();
    }

    private MemoryDatabase memDbMeta;
    private MetaStore metaStore;
    private MetaContext metaContext;

    @Before
    public void setUp() throws Exception
    {
        memDbMeta = new MemoryDatabase("meta");
        memDbMeta.start();

        metaStore = new JDOMetaStore();
        Properties conf = new Properties();
        conf.setProperty("metastore.jdo.connection.drivername", MemoryDatabase.DRIVER_NAME);
        conf.setProperty("metastore.jdo.connection.URL", memDbMeta.CONNECTION_STRING);
        conf.setProperty("metastore.jdo.connection.username", "");
        conf.setProperty("metastore.jdo.connection.password", "");
        metaStore.start(conf);

        metaContext = metaStore.getMetaContext();
        metaContext.addJdbcDataSource(MemoryDatabase.DRIVER_NAME, memDbData.CONNECTION_STRING, memDbData.NAME);
    }

    @After
    public void tearDown() throws Exception
    {
        metaContext.close();
        metaStore.stop();
        memDbMeta.stop();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testUser() throws Exception
    {
        final String name = "octopus";

        assertFalse(metaContext.userExists(name));

        MetaUser user = metaContext.createUser(name, "bitnine");

        assertSame(user, metaContext.getUser(name));

        final String newPassword = "junseok";
        metaContext.alterUser("octopus", newPassword);
        assertEquals(newPassword, user.getPassword());

        final String comment = "superuser";
        metaContext.commentOnUser(comment, name);
        assertEquals(comment, user.getComment());

        MetaUser anon = metaContext.createUser("anon", "null");
        Collection<MetaUser> users = metaContext.getUsers();
        assertTrue(users.contains(user));
        assertTrue(users.contains(anon));
        assertEquals(2, users.size());

        metaContext.dropUser(name);
        assertFalse(metaContext.userExists(name));
    }

    @Test
    public void testGetUserFail() throws Exception
    {
        thrown.expect(MetaException.class);
        thrown.expectMessage("does not exist");
        metaContext.getUser("octopus");
    }

    @Test
    public void testAddDropJdbcDataSource() throws Exception
    {
        MetaDataSource dataSource = metaContext.getDataSource(memDbData.NAME);

        assertEquals(memDbData.NAME, dataSource.getName());

        Collection<MetaSchema> schemas = dataSource.getSchemas();
        assertEquals(1, schemas.size());
        MetaSchema schema = schemas.iterator().next();
        assertEquals(SCHEMA_NAME, schema.getName());

        Map<String, MetaTable> tableMap = new HashMap<>();
        for (MetaTable table : schema.getTables())
            tableMap.put(table.getName(), table);
        assertTrue(tableMap.containsKey("employee"));
        assertTrue(tableMap.containsKey("team"));

        Set<String> columnSet = new HashSet<>();
        for (MetaColumn column : tableMap.get("employee").getColumns())
            columnSet.add(column.getName());
        assertTrue(columnSet.contains("id"));
        assertTrue(columnSet.contains("name"));
        assertTrue(columnSet.contains("permanent"));

        columnSet.clear();
        for (MetaColumn column : tableMap.get("team").getColumns())
            columnSet.add(column.getName());
        assertTrue(columnSet.contains("id"));
        assertTrue(columnSet.contains("name"));
        assertTrue(columnSet.contains("proportion"));
        assertTrue(columnSet.contains("description"));

        MemoryDatabase memDbAnon = new MemoryDatabase("anon");
        memDbAnon.start();
        memDbAnon.importJSON(getClass(), "/sample.json");
        MetaDataSource anon = metaContext.addJdbcDataSource(MemoryDatabase.DRIVER_NAME, memDbAnon.CONNECTION_STRING, memDbAnon.NAME);
        Collection<MetaDataSource> dataSources = metaContext.getDataSources();
        assertTrue(dataSources.contains(dataSource));
        assertTrue(dataSources.contains(anon));
        memDbAnon.stop();

        metaContext.dropJdbcDataSource(memDbData.NAME);
        thrown.expect(MetaException.class);
        thrown.expectMessage("does not exist");
        metaContext.getDataSource(memDbData.NAME);
    }

    @Test
    public void testGetDataSourceFail() throws Exception
    {
        thrown.expect(MetaException.class);
        thrown.expectMessage("does not exist");
        metaContext.getDataSource("any");
    }

    @Test
    public void testDataSourceDescription() throws Exception
    {
        MetaDataSource dataSource = metaContext.getDataSource(memDbData.NAME);

        metaContext.commentOnDataSource("bitnine", memDbData.NAME);
        assertEquals("bitnine", dataSource.getComment());

        JDOMetaContext jdoMetaContext = (JDOMetaContext) metaContext;

        metaContext.commentOnSchema("default", memDbData.NAME, SCHEMA_NAME);
        MetaSchema schema = jdoMetaContext.getSchemaByQualifiedName(memDbData.NAME, SCHEMA_NAME);
        assertEquals("default", schema.getComment());

        metaContext.commentOnTable("Employees", memDbData.NAME, SCHEMA_NAME, "employee");
        MetaTable table = jdoMetaContext.getTableByQualifiedName(memDbData.NAME, SCHEMA_NAME, "employee");
        assertEquals("Employees", table.getComment());

        metaContext.commentOnColumn("Permanent?", memDbData.NAME, SCHEMA_NAME, "employee", "permanent");
        metaContext.setDataCategoryOn("public", memDbData.NAME, SCHEMA_NAME, "employee", "permanent");
        MetaColumn column = jdoMetaContext.getColumnByQualifiedName(memDbData.NAME, SCHEMA_NAME, "employee", "permanent");
        assertEquals("Permanent?", column.getComment());
        assertEquals("public", column.getDataCategory());
    }

    @Test
    public void testGetSchemaByQualifiedNameFail() throws Exception
    {
        thrown.expect(MetaException.class);
        thrown.expectMessage("does not exist");
        ((JDOMetaContext) metaContext).getSchemaByQualifiedName(memDbData.NAME, "any");
    }

    @Test
    public void testGetTableByQualifiedNameFail() throws Exception
    {
        thrown.expect(MetaException.class);
        thrown.expectMessage("does not exist");
        ((JDOMetaContext) metaContext).getTableByQualifiedName(memDbData.NAME, SCHEMA_NAME, "any");
    }

    @Test
    public void testGetColumnByQualifiedNameFail() throws Exception
    {
        thrown.expect(MetaException.class);
        thrown.expectMessage("does not exist");
        ((JDOMetaContext) metaContext).getColumnByQualifiedName(memDbData.NAME, SCHEMA_NAME, "employee", "any");
    }

    @Test
    public void testSystemPrivilege() throws Exception
    {
        MetaUser octopus = metaContext.createUser("octopus", "bitnine");
        MetaUser junseok = metaContext.createUser("junseok", "0009");

        List<SystemPrivilege> sysPrivs = Arrays.asList(SystemPrivilege.values());
        metaContext.addSystemPrivileges(sysPrivs, Arrays.asList(octopus.getName(), junseok.getName()));
        Set<SystemPrivilege> sysPrivSet = new HashSet<>(sysPrivs);
        assertEquals(sysPrivSet, octopus.getSystemPrivileges());
        assertEquals(sysPrivSet, junseok.getSystemPrivileges());

        metaContext.removeSystemPrivileges(
                Arrays.asList(SystemPrivilege.GRANT_ANY_PRIVILEGE, SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE),
                Arrays.asList(octopus.getName(), junseok.getName()));
        sysPrivSet.remove(SystemPrivilege.GRANT_ANY_PRIVILEGE);
        sysPrivSet.remove(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE);
        assertEquals(sysPrivSet, octopus.getSystemPrivileges());
        assertEquals(sysPrivSet, junseok.getSystemPrivileges());
    }

    @Test
    public void testObjectPrivilege() throws Exception
    {
        final String[] schemaName = new String[] {memDbData.NAME, SCHEMA_NAME};

        assertNull(metaContext.getSchemaPrivilege(schemaName, "any"));

        MetaUser octopus = metaContext.createUser("octopus", "bitnine");
        MetaUser junseok = metaContext.createUser("junseok", "0009");

        List<ObjectPrivilege> objPrivs = Arrays.asList(ObjectPrivilege.values());
        metaContext.addObjectPrivileges(
                objPrivs,
                schemaName,
                Arrays.asList(octopus.getName(), junseok.getName()));
        Set<ObjectPrivilege> objPrivSet = new HashSet<>(objPrivs);
        assertEquals(objPrivSet, metaContext.getSchemaPrivilege(schemaName, octopus.getName()).getObjectPrivileges());
        assertEquals(objPrivSet, metaContext.getSchemaPrivilege(schemaName, junseok.getName()).getObjectPrivileges());

        metaContext.removeObjectPrivileges(
                Arrays.asList(ObjectPrivilege.COMMENT),
                schemaName,
                Arrays.asList(octopus.getName(), junseok.getName()));
        objPrivSet.remove(ObjectPrivilege.COMMENT);
        assertEquals(objPrivSet, metaContext.getSchemaPrivilege(schemaName, octopus.getName()).getObjectPrivileges());
        assertEquals(objPrivSet, metaContext.getSchemaPrivilege(schemaName, junseok.getName()).getObjectPrivileges());

        metaContext.removeObjectPrivileges(
                Arrays.asList(ObjectPrivilege.SELECT),
                schemaName,
                Arrays.asList(octopus.getName(), junseok.getName()));
        objPrivSet.remove(ObjectPrivilege.COMMENT);

        assertNull(metaContext.getSchemaPrivilege(schemaName, octopus.getName()));
        assertNull(metaContext.getSchemaPrivilege(schemaName, junseok.getName()));
    }
}
