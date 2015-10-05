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
import org.junit.Test;

import java.io.InputStreamReader;
import java.sql.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MemoryDatabaseTest {
    @Test
    public void testImport() throws Exception {
        MemoryDatabase memDb = new MemoryDatabase("sample");
        memDb.start();

        final String jsonFileName = "/sample.json";
        memDb.importJSON(getClass(), jsonFileName);

        JSONParser jsonParser = new JSONParser();
        JSONArray tables = (JSONArray) jsonParser.parse(new InputStreamReader(getClass().getResourceAsStream(jsonFileName)));
        assertEquals(2, tables.size());

        Connection conn = memDb.getConnection();
        Statement stmt = conn.createStatement();

        JSONObject employee = (JSONObject) tables.get(0);
        ResultSet rs = stmt.executeQuery("SELECT * FROM \"employee\"");
        verifyTableEquals(employee, rs);
        rs.close();

        JSONObject team = (JSONObject) tables.get(1);
        rs = stmt.executeQuery("SELECT * FROM \"team\"");
        verifyTableEquals(team, rs);
        rs.close();

        stmt.close();
        conn.close();

        memDb.stop();
    }

    private void verifyTableEquals(JSONObject expectedTable, ResultSet actualRows) throws Exception {
        ResultSetMetaData actualRowsMetaData = actualRows.getMetaData();

        JSONArray expectedSchema = (JSONArray) expectedTable.get("table-schema");
        assertEquals(expectedSchema.size(), actualRowsMetaData.getColumnCount());
        for (int i = 0; i < expectedSchema.size(); i++)
            assertEquals(expectedSchema.get(i), actualRowsMetaData.getColumnName(i + 1));

        for (Object rowObj : (JSONArray) expectedTable.get("table-rows")) {
            JSONArray row = (JSONArray) rowObj;
            actualRows.next();

            for (int i = 0; i < row.size(); i++) {
                Object expected = row.get(i);
                Object actual;

                int sqlType = actualRowsMetaData.getColumnType(i + 1);
                switch (sqlType) {
                case Types.INTEGER:
                    if (expected instanceof Boolean)
                        expected = (long) ((Boolean) expected ? 1 : 0);
                    actual = actualRows.getLong(i + 1);
                    break;
                case Types.NULL:
                case Types.FLOAT:
                case Types.VARCHAR:
                    actual = actualRows.getObject(i + 1);
                    break;
                default:
                    throw new RuntimeException("java.sql.Types " + sqlType + " is not supported");
                }

                assertEquals(expected, actual);
            }
        }
        assertFalse(actualRows.next());
    }
}
