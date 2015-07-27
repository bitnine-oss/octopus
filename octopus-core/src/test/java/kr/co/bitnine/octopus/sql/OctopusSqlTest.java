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

import org.junit.Test;

import java.util.List;

public class OctopusSqlTest
{
    @Test
    public void test() throws Exception
    {
        String query = "CREATE USER octopus IDENTIFIED BY 'bitnine';" +
                "ALTER SYSTEM ADD DATASOURCE `bitnine` CONNECT BY 'jdbc:sqlite:file::memory:?cache=shared';" +
                "DROP USER octopus;" +
                "ALTER USER octopus IDENTIFIED BY 'bitnine0' 'bitnine'";
        List<OctopusSqlCommand> commands = OctopusSql.parse(query);

        OctopusSqlRunner runner = new OctopusSqlRunner() {
            @Override
            public void createUser(String name, String password) throws Exception
            {
                System.out.println("name=" + name + ", password=" + password);
            }

            @Override
            public void addDatasource(String datasourceName, String jdbcConnectionString) throws Exception
            {
                System.out.println("name=" + datasourceName + ", jdbcConnectionString=" + jdbcConnectionString);
            }

            @Override
            public void dropUser(String name) throws Exception
            {
                System.out.println("name=" + name);
            }

            @Override
            public void alterUser(String name, String password, String old_password) throws Exception
            {
                System.out.println("name=" + name + ", password=" + password + ", old_password=" + old_password);
            }
        };

        for (OctopusSqlCommand c : commands)
            OctopusSql.run(c, runner);
    }
}