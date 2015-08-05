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

package kr.co.bitnine.octopus;

import org.postgresql.jdbc4.Jdbc4Connection;
import org.postgresql.util.HostSpec;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

public class OctopusConnection extends Jdbc4Connection
{
    public OctopusConnection(HostSpec[] hostSpecs, String user, String database, Properties info, String url) throws SQLException
    {
        super(hostSpecs, user, database, info, url);
    }

    /*
     * We override getMetaData() to return OctopusDatabaseMetaData.
     * This is the main reason why we made this class.
     */
    public DatabaseMetaData getMetaData() throws SQLException
    {
        checkClosed();
        if (metadata == null)
            metadata = new OctopusDatabaseMetaData(this);
        return metadata;
    }
}
