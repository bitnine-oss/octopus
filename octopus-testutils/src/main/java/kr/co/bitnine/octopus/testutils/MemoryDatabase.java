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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
    }

    public void start() throws SQLException
    {
        initialConnection = DriverManager.getConnection(CONNECTION_STRING);
    }

    public void stop() throws SQLException
    {
        initialConnection.close();
    }

    // XXX: temporary
    public void init() throws SQLException
    {
        Statement stmt = initialConnection.createStatement();
        stmt.executeUpdate("CREATE TABLE BITNINE (ID INTEGER, NAME STRING)");
        stmt.executeUpdate("INSERT INTO BITNINE VALUES(9, 'jsyang')");
        stmt.close();
    }
}
