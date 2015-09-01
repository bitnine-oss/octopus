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

public interface OctopusSqlRunner
{
    void addDataSource(String dataSourceName, String jdbcConnectionString) throws Exception;
    void createUser(String name, String password) throws Exception;
    void alterUser(String name, String password, String oldPassword) throws Exception;
    void dropUser(String name) throws Exception;
    void createRole(String role) throws Exception;
    void dropRole(String role) throws Exception;
    TupleSet showDataSources() throws Exception;
    TupleSet showSchemas(String dataSource, String schemaPattern) throws Exception;
    TupleSet showTables(String dataSource, String schemaPattern, String tablePattern) throws Exception;
    TupleSet showColumns(String dataSource, String schemaPattern, String tablePattern, String columnPattern) throws Exception;
    TupleSet showTablePrivileges(String dataSource, String schemaPattern, String tablePattern) throws Exception;
    TupleSet showColumnPrivileges(String dataSource, String schemaPattern, String tablePattern, String columnPattern) throws Exception;
    TupleSet showUsers() throws Exception;
}

