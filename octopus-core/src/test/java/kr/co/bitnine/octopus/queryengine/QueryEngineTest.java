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

import kr.co.bitnine.octopus.schema.Database;
import kr.co.bitnine.octopus.schema.MetaStore;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.Frameworks;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class QueryEngineTest
{
    private static final String SQLITE_URL = "jdbc:sqlite:file::memory:?cache=shared";

    private Connection initialConnection;
    private MetaStore metastore;

    @Before
    public void setUp() throws Exception
    {
        Class.forName("org.sqlite.JDBC");

        initialConnection = DriverManager.getConnection(SQLITE_URL);

        Statement stmt = initialConnection.createStatement();
        stmt.executeUpdate("CREATE TABLE BITNINE (id INTEGER, name STRING)");
        stmt.executeUpdate("INSERT INTO BITNINE VALUES(9, 'jsyang')");

        metastore = new MetaStore();
    }

    @After
    public void tearDown() throws Exception
    {
        initialConnection.close();
    }

    @Test
    public void test() throws Exception
    {
        Connection conn = DriverManager.getConnection(SQLITE_URL);

        Database db = new Database(conn);
        //metastore.add("SQLITE", db);

        QueryEngine queryEngine = new QueryEngine(metastore.getSchema());

        conn.close();

        queryEngine.executeQuery("SELECT * FROM SQLITE.__DEFAULT.BITNINE");
    }
}
