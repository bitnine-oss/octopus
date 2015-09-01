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

import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import org.junit.Test;

import java.util.List;

public class OctopusSqlTest
{
    @Test
    public void test() throws Exception
    {
        String query = "CREATE USER octopus IDENTIFIED BY 'bitnine';\n" +
                "ALTER SYSTEM ADD DATASOURCE `bitnine` CONNECT BY 'jdbc:sqlite:file::memory:?cache=shared';\n" +
                "DROP USER octopus;\n" +
                "ALTER USER octopus IDENTIFIED BY 'bitnine';\n" +
                "CREATE ROLE octopus;\n" +
                "DROP ROLE octopus;\n";
        List<OctopusSqlCommand> commands = OctopusSql.parse(query);

        OctopusSqlRunner runner = new OctopusSqlRunner() {
            @Override
            public void addDataSource(String dataSourceName, String jdbcConnectionString) throws Exception
            {
                System.out.println("name=" + dataSourceName + ", jdbcConnectionString=" + jdbcConnectionString);
            }

            @Override
            public void createUser(String name, String password) throws Exception
            {
                System.out.println("name=" + name + ", password=" + password);
            }

            @Override
            public void alterUser(String name, String password, String old_password) throws Exception
            {
                System.out.println("name=" + name + ", password=" + password + ", old_password=" + old_password);
            }

            @Override
            public void dropUser(String name) throws Exception
            {
                System.out.println("name=" + name);
            }

            @Override
            public TupleSet showUsers() throws Exception
            {
                System.out.println("SHOW USERS");
                return null;
            }

            @Override
            public void createRole(String role) throws Exception
            {
                System.out.println("role=" + role);
            }

            @Override
            public void dropRole(String role) throws Exception
            {
                System.out.println("role=" + role);
            }

            @Override
            public TupleSet showDataSources() throws Exception
            {
                System.out.println("SHOW DATASOURCES");
                return null;
            }

            @Override
            public TupleSet showSchemas(String dataSource, String schemaPattern) throws Exception
            {
                return null;
            }

            @Override
            public TupleSet showTables(String dataSource, String schemaPattern, String tablePattern) throws Exception
            {
                System.out.println("dataSource=" + dataSource + ", schemaPattern=" + schemaPattern + ", tablePattern=" + tablePattern);
                return null;
            }

            @Override
            public TupleSet showColumns(String dataSource, String schemaPattern, String tablePattern, String columnPattern) throws Exception
            {
                return null;
            }

            @Override
            public TupleSet showTablePrivileges(String dataSource, String schemaPattern, String tablePattern) throws Exception
            {
                return null;
            }

            @Override
            public TupleSet showColumnPrivileges(String dataSource, String schemaPattern, String tablePattern, String columnPattern) throws Exception
            {
                return null;
            }
        };

        for (OctopusSqlCommand c : commands)
            OctopusSql.run(c, runner);
    }
}