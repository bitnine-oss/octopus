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

package kr.co.bitnine.octopus.testutils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.sql.*;

public class MemoryDatabase
{
    public static final String DRIVER_NAME = "org.sqlite.JDBC";

    public final String CONNECTION_STRING;
    public final String NAME;

    private Connection initialConnection;

    public MemoryDatabase(String name) throws ClassNotFoundException
    {
        Class.forName(DRIVER_NAME);

        CONNECTION_STRING = "jdbc:sqlite:file:" + name + "?mode=memory&cache=shared";
        NAME = name;

        initialConnection = null;
    }

    public synchronized void start() throws SQLException
    {
        if (initialConnection == null)
            initialConnection = DriverManager.getConnection(CONNECTION_STRING);
    }

    public synchronized void stop() throws SQLException
    {
        if (initialConnection != null) {
            initialConnection.close();
            initialConnection = null;
        }
    }

    public Connection getConnection() throws Exception
    {
        return DriverManager.getConnection(CONNECTION_STRING);
    }

    public void importJSON(Class<?> clazz, String resourceName) throws Exception
    {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        Statement stmt = conn.createStatement();

        JSONParser jsonParser = new JSONParser();
        JSONArray tables = (JSONArray) jsonParser.parse(new InputStreamReader(clazz.getResourceAsStream(resourceName)));
        for (Object tableObj : tables) {
            StringBuilder queryBuilder = new StringBuilder();

            JSONObject table = (JSONObject) tableObj;

            String tableName = jsonValueToSqlIdent(table.get("table-name"));
            queryBuilder.append("CREATE TABLE ").append(tableName).append('(');
            JSONArray schema = (JSONArray) table.get("table-schema");
            for (Object columnNameObj : schema) {
                String columnName = jsonValueToSqlValue(columnNameObj);
                queryBuilder.append(columnName).append(',');
            }
            queryBuilder.setCharAt(queryBuilder.length() - 1, ')');
            stmt.execute(queryBuilder.toString());

            JSONArray rows = (JSONArray) table.get("table-rows");
            for (Object rowObj : rows) {
                JSONArray row = (JSONArray) rowObj;

                queryBuilder.setLength(0);
                queryBuilder.append("INSERT INTO ").append(tableName).append(" VALUES(");
                for (Object columnObj: row)
                    queryBuilder.append(jsonValueToSqlValue(columnObj)).append(',');
                queryBuilder.setCharAt(queryBuilder.length() - 1, ')');
                stmt.executeUpdate(queryBuilder.toString());
            }
        }

        stmt.close();
        conn.commit();
        conn.close();
    }

    private String jsonValueToSqlIdent(Object jsonValue)
    {
        if (jsonValue instanceof String)
            return '"' + jsonValue.toString() + '"';

        String typeName = jsonValue.getClass().getSimpleName();
        throw new IllegalArgumentException("type '" + typeName + "' of JSON value is invalid for identifier");
    }

    private String jsonValueToSqlValue(Object jsonValue)
    {
        if (jsonValue == null)
            return "NULL";

        if (jsonValue instanceof Boolean)
            return (Boolean) jsonValue ? "1" : "0";

        if (jsonValue instanceof String)
            return "'" + jsonValue.toString() + "'";

        if (jsonValue instanceof Long || jsonValue instanceof Double)
            return jsonValue.toString();

        String typeName = jsonValue.getClass().getSimpleName();
        throw new IllegalArgumentException("type '" + typeName + "' of JSON value is not supported");
    }

    public void runExecuteUpdate(String sqlStmt) throws SQLException
    {
        Statement stmt = initialConnection.createStatement();
        stmt.executeUpdate(sqlStmt);
        stmt.close();
    }
}
