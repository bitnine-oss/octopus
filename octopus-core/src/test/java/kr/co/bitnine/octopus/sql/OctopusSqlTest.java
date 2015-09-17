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

package kr.co.bitnine.octopus.sql;

import kr.co.bitnine.octopus.meta.privilege.ObjectPrivilege;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class OctopusSqlTest
{
    private static OctopusSqlRunner runner;


    @BeforeClass
    public static void setUpClass()
    {
        runner = new OctopusSqlRunner() {
            @Override
            public void addDataSource(String dataSourceName, String jdbcConnectionString) throws Exception
            {
                System.out.println("ADD DATASOURCE name=" + dataSourceName + ", jdbcConnectionString=" + jdbcConnectionString);
            }

            @Override
            public void updateDataSource(String dataSourceName) throws Exception
            {
                System.out.println("UPDATE DATASOURCE name=" + dataSourceName);
            }

            @Override
            public void dropDataSource(String dataSourceName) throws Exception
            {
                System.out.println("DROP DATASOURCE name=" + dataSourceName);
            }

            @Override
            public void createUser(String name, String password) throws Exception
            {
                System.out.println("CREATE USER name=" + name + ", password=" + password);
            }

            @Override
            public void alterUser(String name, String password, String oldPassword) throws Exception
            {
                System.out.println("ALTER USER name=" + name + ", password=" + password + ", oldPassword=" + oldPassword);
            }

            @Override
            public void dropUser(String name) throws Exception
            {
                System.out.println("DROP USER " + name);
            }

            @Override
            public void createRole(String role) throws Exception
            {
                System.out.println("CREATE ROLE " + role);
            }

            @Override
            public void dropRole(String role) throws Exception
            {
                System.out.println("DROP ROLE " + role);
            }

            @Override
            public void grantSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> grantees)
            {
                System.out.print("GRANT [");
                for (SystemPrivilege sysPriv : sysPrivs)
                    System.out.print(sysPriv.name() + ",");
                System.out.print("] TO [");
                for (String grantee : grantees)
                    System.out.print(grantee + ",");
                System.out.println("]");
            }

            @Override
            public void revokeSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> revokees)
            {
                System.out.print("REVOKE [");
                for (SystemPrivilege sysPriv : sysPrivs)
                    System.out.print(sysPriv.name() + ",");
                System.out.print("] FROM [");
                for (String revokee : revokees)
                    System.out.print(revokee + ",");
                System.out.println("]");
            }

            @Override
            public void grantObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] objName, List<String> grantees) throws Exception
            {
                System.out.print("GRANT [");
                for (ObjectPrivilege objPriv : objPrivs)
                    System.out.print(objPriv.name() + ",");
                System.out.print("] ON [" + objName[0] + "." + objName[1] + "] TO [");
                for (String grantee : grantees)
                    System.out.print(grantee + ",");
                System.out.println("]");
            }

            @Override
            public void revokeObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] objName, List<String> revokees) throws Exception
            {
                System.out.print("REVOKE [");
                for (ObjectPrivilege objPriv : objPrivs)
                    System.out.print(objPriv.name() + ",");
                System.out.print("] ON [" + objName[0] + "." + objName[1] + "] FROM [");
                for (String revokee : revokees)
                    System.out.print(revokee + ",");
                System.out.println("]");
            }

            @Override
            public TupleSet showDataSources() throws Exception
            {
                System.out.println("SHOW DATASOURCES");
                return null;
            }

            @Override
            public TupleSet showSchemas(String dataSourceName, String schemaPattern) throws Exception
            {
                System.out.println("SHOW SCHEMAS " + dataSourceName + "/" + schemaPattern);
                return null;
            }

            @Override
            public TupleSet showTables(String dataSourceName, String schemaPattern, String tablePattern) throws Exception
            {
                System.out.println("SHOW TABLES " + dataSourceName + "/" + schemaPattern + "/" + tablePattern);
                return null;
            }

            @Override
            public TupleSet showColumns(String dataSourceName, String schemaPattern, String tablePattern, String columnPattern) throws Exception
            {
                System.out.println("SHOW COLUMNS " + dataSourceName + "/" + schemaPattern + "/" + tablePattern + "/" + columnPattern);
                return null;
            }

            @Override
            public TupleSet showTablePrivileges(String dataSourceName, String schemaPattern, String tablePattern) throws Exception
            {
                System.out.println("SHOW TABLE PRIVILEGES " + dataSourceName + "/" + schemaPattern + "/" + tablePattern);
                return null;
            }

            @Override
            public TupleSet showColumnPrivileges(String dataSourceName, String schemaPattern, String tablePattern, String columnPattern) throws Exception
            {
                System.out.println("SHOW COLUMN PRIVILEGES " + dataSourceName + "/" + schemaPattern + "/" + tablePattern + "/" + columnPattern);
                return null;
            }

            @Override
            public TupleSet showAllUsers() throws Exception
            {
                System.out.println("SHOW ALL USERS");
                return null;
            }

            @Override
            public TupleSet showObjPrivsFor(String userName) throws Exception
            {
                System.out.println("SHOW OBJECT PRIVILEGES FOR " + userName);
                return null;
            }

            @Override
            public void commentOn(OctopusSqlCommentTarget target, String comment) throws Exception
            {
                System.out.println("COMMENT ON targetType=" + target.type.name() + " dataSourceName=" + target.dataSource + " schemaName=" + target.schema + " tableName=" + target.table + " columnName=" + target.column + " user=" + target.user);
            }

            @Override
            public void setDataCategoryOn(String dataSource, String schema, String table, String column, String category) throws Exception
            {
                System.out.println("SET DATACATEGORY ON dataSourceName=" + dataSource + " schemaName=" + schema + " tableName=" + table + " columnName=" + column + " category=" + category);
            }
        };
    }

    private void parseAndRun(String query) throws Exception
    {
        for (OctopusSqlCommand c : OctopusSql.parse(query))
            OctopusSql.run(c, runner);
    }

    @Test
    public void testAlterSystem() throws Exception
    {
        String query = "ALTER SYSTEM ADD DATASOURCE `bitnine` CONNECT BY 'jdbc:sqlite:file::memory:?cache=shared';\n";
        parseAndRun(query);
    }

    @Test
    public void testUpdateDatasource() throws Exception
    {
        String query = "ALTER SYSTEM UPDATE DATASOURCE `bitnine`;\n";
        parseAndRun(query);
    }

    @Test
    public void testDropDatasource() throws Exception
    {
        String query = "ALTER SYSTEM DROP DATASOURCE `bitnine`;\n";
        parseAndRun(query);
    }

    @Test
    public void testUser() throws Exception
    {
        String query = "CREATE USER octopus IDENTIFIED BY 'bitnine';\n" +
                "ALTER USER octopus IDENTIFIED BY 'bitnine';\n" +
                "DROP USER octopus;\n";
        parseAndRun(query);
    }

    @Test
    public void testRole() throws Exception
    {
        String query = "CREATE ROLE octopus;\n" +
                "DROP ROLE octopus;\n";
        parseAndRun(query);
    }

    @Test
    public void testGrantRevokeSysPrivs() throws Exception
    {
        String query = "GRANT GRANT ANY OBJECT PRIVILEGE, GRANT ANY PRIVILEGE TO octopus, jsyang;\n" +
                "REVOKE ALL PRIVILEGES FROM octopus;\n";
        parseAndRun(query);
    }

    @Test
    public void testGrantRevokeObjPrivs() throws Exception
    {
        String query = "GRANT SELECT, COMMENT ON \"dataSource\".\"schema\" TO octopus, jsyang;\n" +
                "REVOKE ALL ON \"dataSource\".\"schema\" FROM octopus;\n";
        parseAndRun(query);
    }

    @Test
    public void testShow() throws Exception
    {
        String query = "SHOW DATASOURCES;\n" +
                "SHOW SCHEMAS SCHEMA 'SCHMEA_';\n" +
                "SHOW TABLES DATASOURCE DS1 TABLE 'TBL%';\n" +
                "SHOW COLUMNS COLUMN '%\\_COL';\n" +
                "SHOW TABLE PRIVILEGES DATASOURCE DS1;\n" +
                "SHOW COLUMN PRIVILEGES COLUMN '_COL\\%';\n" +
                "SHOW ALL USERS;\n" +
                "SHOW OBJECT PRIVILEGES FOR jsyang;\n";
        parseAndRun(query);
    }

    @Test
    public void testCommentOn() throws Exception
    {
        String query = "COMMENT ON DATASOURCE DS1 IS 'test';\n" +
                "COMMENT ON SCHEMA DS1.SCHEMA1 IS 'test';\n" +
                "COMMENT ON TABLE DS1.SCHEMA1.TABLE1 IS 'test';\n" +
                "COMMENT ON COLUMN DS1.SCHEMA1.TABLE1.COLUMN1 IS 'test';\n" +
                "COMMENT ON USER USER1 IS 'test';\n";
        parseAndRun(query);
    }

    @Test
    public void testSetDataCategoryOn() throws Exception
    {
        String query = "SET DATACATEGORY ON COLUMN DS1.SCHEMA1.TABLE1.COLUMN1 IS 'category';\n";
        parseAndRun(query);
    }
}