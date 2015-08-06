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

import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.MetaStoreService;
import kr.co.bitnine.octopus.meta.MetaStores;
import kr.co.bitnine.octopus.schema.SchemaManager;
import kr.co.bitnine.octopus.testutils.MemoryDatabase;
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
    private MemoryDatabase metaMemDb;
    private MemoryDatabase dataMemDb;

    @Before
    public void setUp() throws Exception
    {
        Class.forName("kr.co.bitnine.octopus.Driver");

        metaMemDb = new MemoryDatabase("META");
        metaMemDb.start();

        dataMemDb = new MemoryDatabase("DATA");
        dataMemDb.start();
        dataMemDb.init();
    }

    @After
    public void tearDown() throws Exception
    {
        dataMemDb.stop();
        metaMemDb.stop();
    }

    @Test
    public void testStartup() throws Exception
    {
        Configuration conf = new OctopusConfiguration();
        conf.set("metastore.jdo.connection.drivername", MemoryDatabase.DRIVER_NAME);
        conf.set("metastore.jdo.connection.URL", metaMemDb.CONNECTION_STRING);
        conf.set("metastore.jdo.connection.username", "");
        conf.set("metastore.jdo.connection.password", "");

        MetaStore metaStore = MetaStores.newInstance(conf.get("metastore.class"));
        MetaStoreService metaStoreService = new MetaStoreService(metaStore);
        metaStoreService.init(conf);
        metaStoreService.start();

        SchemaManager schemaManager = new SchemaManager(metaStore);
        schemaManager.init(conf);
        schemaManager.start();

        SessionServer server = new SessionServer(metaStore, schemaManager);
        server.init(conf);
        server.start();

        InetSocketAddress addr = NetUtils.createSocketAddr("127.0.0.1:58000");
        String url = "jdbc:octopus://" + NetUtils.getHostPortString(addr) + "/db";

        Properties info = new Properties();
        info.setProperty("user", "octopus");
        info.setProperty("password", "bitnine");

        Connection conn = DriverManager.getConnection(url, info);
        assertFalse(conn.isClosed());

        String query = "ALTER SYSTEM ADD DATASOURCE " + dataMemDb.NAME + " CONNECT BY '" + dataMemDb.CONNECTION_STRING + "'";
        Statement stmt = conn.createStatement();
        stmt.execute(query);

        query = "SELECT ID, NAME FROM BITNINE;";
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println("id=" + id + ", name=" + name);
        }
        rs.close();
        stmt.close();

        query = "CREATE USER jsyang IDENTIFIED BY '0009';";
        stmt = conn.createStatement();
        stmt.execute(query);
        stmt.close();

        conn.close();

        info.setProperty("user", "jsyang");
        info.setProperty("password", "0009");
        conn = DriverManager.getConnection(url, info);
        assertFalse(conn.isClosed());

        query = "ALTER USER jsyang IDENTIFIED BY 'jsyang' REPLACE '0009';";
        stmt = conn.createStatement();
        stmt.execute(query);
        stmt.close();

        conn.close();

        info.setProperty("user", "jsyang");
        info.setProperty("password", "jsyang");
        conn = DriverManager.getConnection(url, info);
        assertFalse(conn.isClosed());

        conn.close();

        info.setProperty("user", "octopus");
        info.setProperty("password", "bitnine");
        conn = DriverManager.getConnection(url, info);
        assertFalse(conn.isClosed());

        query = "DROP USER jsyang;";
        stmt = conn.createStatement();
        stmt.execute(query);
        stmt.close();

        conn.close();

        info.setProperty("user", "octopus");
        info.setProperty("password", "bitnine");
        conn = DriverManager.getConnection(url, info);
        assertFalse(conn.isClosed());

        query = "CREATE ROLE bmkim;";
        stmt = conn.createStatement();
        stmt.execute(query);
        stmt.close();

        conn.close();

        info.setProperty("user", "octopus");
        info.setProperty("password", "bitnine");
        conn = DriverManager.getConnection(url, info);
        assertFalse(conn.isClosed());

        query = "DROP ROLE bmkim;";
        stmt = conn.createStatement();
        stmt.execute(query);
        stmt.close();

        conn.close();

        server.stop();
        schemaManager.stop();
        metaStoreService.stop();
    }
}
