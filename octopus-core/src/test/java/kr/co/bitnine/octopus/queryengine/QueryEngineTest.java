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

import kr.co.bitnine.octopus.TestDb;
import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.schema.MetaStore;

import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

public class QueryEngineTest
{
    private TestDb testDb;

    @Before
    public void setUp() throws Exception
    {
        testDb = new TestDb();
        testDb.create();
    }

    @After
    public void tearDown() throws Exception
    {
        testDb.destroy();
    }

    @Test
    public void test() throws Exception
    {
        Configuration conf = new OctopusConfiguration();
        testDb.setMetaStoreConf(conf);

        MetaStore.init(conf);
        MetaStore metaStore = MetaStore.get();
        Connection conn = testDb.getTestDbConnection();
        metaStore.addDataSource("SQLITE", testDb.getDriverName(), testDb.getTestDbURL(), conn, "test database");
/*
        QueryEngine queryEngine = new QueryEngine(metaStore);
        ParsedStatement ps = queryEngine.parse("SELECT ID, NAME FROM SQLITE.__DEFAULT.BITNINE", null);
        ExecutableStatement es = queryEngine.bind(ps, null, null, null);
        queryEngine.execute(es, 0);

//        queryEngine.executeQuery("SELECT ID FROM SQLITE.__DEFAULT.BITNINE WHERE id IN (SELECT id FROM SQLITE.__DEFAULT.BITNINE)");
 */
        conn.close();
        metaStore.destroy();
    }
}
