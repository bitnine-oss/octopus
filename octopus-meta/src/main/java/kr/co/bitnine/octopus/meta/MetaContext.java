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
import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.meta.model.MetaUser;

import java.util.Collection;

public interface MetaContext
{
    boolean userExists(String name) throws MetaException;
    MetaUser createUser(String name, String password) throws MetaException;
    String getUserPasswordByName(String name) throws MetaException;
    void alterUser(String name, String newPassword) throws MetaException;
    void dropUser(String name) throws MetaException;

    MetaDataSource addJdbcDataSource(String driverName, String connectionString, String name) throws MetaException;
    Collection<MetaDataSource> getDataSources() throws MetaException;
    MetaDataSource getDataSourceByName(String name) throws MetaException;

    MetaTable getTableByName(String name) throws MetaException;

    void close();
}
