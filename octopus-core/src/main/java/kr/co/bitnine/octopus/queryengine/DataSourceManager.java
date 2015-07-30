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

package kr.co.bitnine.octopus.queryengine;

import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;

class DataSourceManager
{
    private MetaStore metaStore;

    DataSourceManager(MetaStore metaStore)
    {
        this.metaStore = metaStore;
    }
/*
    Connection getDataSourceConnection(String dataSourceName) throws Exception
    {
        // TODO: connection pooling

        // suppose that we can connect to the data source via JDBC connection
        MetaDataSource dataSource = metaStore.getDataSource(dataSourceName);

        return getJDBCconnection(dataSource.getDriver(), dataSource.getConnectionString());
    }

    private Connection getJDBCconnection(String driver, String connectionString) throws Exception
    {
        Class.forName(driver);
        return DriverManager.getConnection(connectionString);
    }
 */
}
