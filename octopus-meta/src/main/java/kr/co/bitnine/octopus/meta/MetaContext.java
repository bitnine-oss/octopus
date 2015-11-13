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

package kr.co.bitnine.octopus.meta;

import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.meta.model.MetaRole;
import kr.co.bitnine.octopus.meta.model.MetaSchemaPrivilege;
import kr.co.bitnine.octopus.meta.model.MetaUser;
import kr.co.bitnine.octopus.meta.privilege.ObjectPrivilege;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;

import java.util.Collection;
import java.util.List;

public interface MetaContext {
    /*
     * User
     */

    boolean userExists(String name) throws MetaException;

    MetaUser getUser(String name) throws MetaException;

    MetaUser createUser(String name, String password) throws MetaException;

    void alterUser(String name, String newPassword) throws MetaException;

    void dropUser(String name) throws MetaException;

    void commentOnUser(String comment, String name) throws MetaException;

    Collection<MetaUser> getUsers() throws MetaException;

    /*
     * DataSource
     */

    boolean dataSourceExists(String name) throws MetaException;

    MetaDataSource getDataSource(String name) throws MetaException;

    MetaDataSource addJdbcDataSource(String driverName, String connectionString, String name) throws MetaException;

    void dropJdbcDataSource(String name) throws MetaException;

    MetaDataSource updateJdbcDataSource(String dataSourceName, final String schemaRegex, final String tableRegex) throws MetaException;

    void commentOnDataSource(String comment, String name) throws MetaException;

    Collection<MetaDataSource> getDataSources() throws MetaException;

    Collection<ResultOfGetColumns> getColumns(String datasourceName, final String schemaRegex, final String tableRegex, final String columnRegex) throws MetaException;
    /*
     * Schema
     */

    void commentOnSchema(String comment, String dataSourceName, String schemaName) throws MetaException;

    /*
     * Table
     */

    void commentOnTable(String comment, String dataSourceName, String schemaName, String tableName) throws MetaException;

    /*
     * Column
     */

    void commentOnColumn(String comment, String dataSourceName, String schemaName, String tableName, String columnName) throws MetaException;

    void setDataCategoryOn(String category, String dataSource, String schema, String table, String column) throws MetaException;

    /*
     * Role
     */

    MetaRole createRole(String name) throws MetaException;

    void dropRoleByName(String name) throws MetaException;

    /*
     * Privilege
     */

    void addSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> userNames) throws MetaException;

    void removeSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> userNames) throws MetaException;

    MetaSchemaPrivilege getSchemaPrivilege(String[] schemaName, String userName) throws MetaException;

    Collection<MetaSchemaPrivilege> getSchemaPrivilegesByUser(String userName) throws MetaException;

    void addObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] schemaName, List<String> userNames) throws MetaException;

    void removeObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] schemaName, List<String> userNames) throws MetaException;

    void close();
}
