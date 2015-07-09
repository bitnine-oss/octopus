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

package kr.co.bitnine.octopus.frame;

import kr.co.bitnine.octopus.TestDb;
import kr.co.bitnine.octopus.conf.OctopusConfiguration;

import kr.co.bitnine.octopus.schema.MetaStore;
import kr.co.bitnine.octopus.util.NetUtils;
import org.apache.hadoop.conf.Configuration;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class SessionServerTest
{
    private TestDb testDb;

    @Before
    public void setUp() throws Exception
    {
        Class.forName("org.postgresql.Driver");

        testDb = new TestDb();
        testDb.create();
    }

    @After
    public void tearDown() throws Exception
    {
        testDb.destroy();
    }

    @Test
    public void testStartup() throws Exception
    {
        Configuration conf = new OctopusConfiguration();
        testDb.setMetaStoreConf(conf);

        MetaStore.init(conf);
        MetaStore metaStore = MetaStore.get();
        Connection testConn = testDb.getTestDbConnection();
        metaStore.addDataSource("SQLITE", testDb.getDriverName(), testDb.getTestDbURL(), testConn, "test database");

        SessionServer server = new SessionServer();
        server.init(conf);
        server.start();

        InetSocketAddress addr = NetUtils.createSocketAddr("127.0.0.1:58000");
        String url = "jdbc:postgresql://" + NetUtils.getHostPortString(addr) + "/db";

        Properties info = new Properties();
        info.setProperty("user", "octopus");
        info.setProperty("password", "bitnine");

        Connection conn = DriverManager.getConnection(url, info);
        assertFalse(conn.isClosed());

/*
        String query = "SELECT ID, NAME FROM SQLITE.__DEFAULT.BITNINE";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println("id=" + id + ", name=" + name);
        }
 */

        String query = "CREATE USER jsyang IDENTIFIED BY '0009';";
        Statement stmt = conn.createStatement();
        stmt.execute(query);
//        assertTrue(stmt.execute(query));

        conn.close();

        info.setProperty("user", "jsyang");
        info.setProperty("password", "0009");
        conn = DriverManager.getConnection(url, info);
        assertFalse(conn.isClosed());

        conn.close();

        server.stop();

        testConn.close();
        metaStore.destroy();

        testDb.destroy();
    }
}
