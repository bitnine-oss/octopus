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

import org.postgresql.jdbc4.AbstractJdbc4DatabaseMetaData;
import org.postgresql.jdbc4.Jdbc4Connection;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

// XXX: Why we need to extend AbstractJdbc4DatabaseMetaData?
public class OctopusDatabaseMetaData extends AbstractJdbc4DatabaseMetaData implements DatabaseMetaData
{
    public OctopusDatabaseMetaData(Jdbc4Connection conn)
    {
        super(conn);
    }

    public java.sql.ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) throws SQLException
    {
        return null;
    }
}
