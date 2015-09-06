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

import java.util.List;

public interface OctopusSqlRunner
{
    void addDataSource(String dataSourceName, String jdbcConnectionString) throws Exception;
    void createUser(String name, String password) throws Exception;
    void alterUser(String name, String password, String oldPassword) throws Exception;
    void dropUser(String name) throws Exception;
    void createRole(String role) throws Exception;
    void dropRole(String role) throws Exception;
    void grantSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> grantees) throws Exception;
    void revokeSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> revokees) throws Exception;
    void grantObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] objName, List<String> grantees) throws Exception;
    void revokeObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] objName, List<String> revokees) throws Exception;
    TupleSet showDataSources() throws Exception;
    TupleSet showSchemas(String dataSourceName, String schemaPattern) throws Exception;
    TupleSet showTables(String dataSourceName, String schemaPattern, String tablePattern) throws Exception;
    TupleSet showColumns(String dataSourceName, String schemaPattern, String tablePattern, String columnPattern) throws Exception;
    TupleSet showTablePrivileges(String dataSourceName, String schemaPattern, String tablePattern) throws Exception;
    TupleSet showColumnPrivileges(String dataSourceName, String schemaPattern, String tablePattern, String columnPattern) throws Exception;
    TupleSet showAllUsers() throws Exception;
    TupleSet showObjPrivsFor(String userName) throws Exception;
    void commentOn(OctopusSqlCommentTarget target, String comment) throws Exception;
    void setDataCategoryOn(String dataSource, String schema, String table, String column, String category) throws Exception;
}

