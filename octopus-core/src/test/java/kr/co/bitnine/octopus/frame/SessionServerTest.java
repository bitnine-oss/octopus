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

import kr.co.bitnine.octopus.util.NetUtils;
import org.apache.hadoop.conf.Configuration;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class SessionServerTest
{
    @Before
    public void setUp() throws Exception
    {
        Class.forName("org.postgresql.Driver");
    }

    @Test
    public void testStartup() throws Exception
    {
        SessionServer server = new SessionServer();
        Configuration conf = new OctopusConfiguration();
        server.init(conf);
        server.start();

        InetSocketAddress addr = NetUtils.createSocketAddr("127.0.0.1:58000");
        String url = "jdbc:postgresql://" + NetUtils.getHostPortString(addr) + "/db";

        Properties info = new Properties();
        info.setProperty("user", "octopus");
        info.setProperty("password", "bitnine");

        Connection db = DriverManager.getConnection(url, info);
        assertFalse(db.isClosed());
        db.close();

        server.stop();
    }
}
