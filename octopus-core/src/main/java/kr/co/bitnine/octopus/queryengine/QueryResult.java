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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryResult
{
    ResultSet resultSet;

    QueryResult(ResultSet resultSet)
    {
        this.resultSet = resultSet;
    }

    public void close() throws SQLException
    {
        Statement stmt = resultSet.getStatement();
        /*
         * If the result set was generated some other way,
         * such as by a DatabaseMetaData method, this method may return null.
         */
        if (stmt == null) {
            resultSet.close();
            return;
        }

        Connection conn = stmt.getConnection();

        resultSet.close();
        stmt.close();
        conn.close();
    }

    public <T> T unwrap(Class<T> clazz)
    {
        if (clazz.isInstance(this))
            return clazz.cast(this);
        if (clazz.isInstance(resultSet))
            return clazz.cast(resultSet);
        throw new ClassCastException("not a " + clazz);
    }
}
