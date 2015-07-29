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
import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.MetaStoreService;
import kr.co.bitnine.octopus.meta.MetaStores;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.schema.SchemaManager;
import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;

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

        MetaStore metaStore = MetaStores.newInstance(conf.get("metastore.class"));
        MetaStoreService metaStoreService = new MetaStoreService(metaStore);
        metaStoreService.init(conf);
        metaStoreService.start();

        SchemaManager schemaManager = new SchemaManager(metaStore);
        schemaManager.init(conf);
        schemaManager.start();

        MetaContext mc = metaStore.getMetaContext();

        MetaDataSource metaDataSource = mc.addJdbcDataSource(testDb.getDriverName(), testDb.getTestDbURL(), "SQLITE");
        schemaManager.addDataSource(metaDataSource);

        QueryEngine queryEngine = new QueryEngine(mc, schemaManager);
        ParsedStatement ps = queryEngine.parse("SELECT ID, NAME FROM BITNINE", null);
        ExecutableStatement es = queryEngine.bind(ps, null, null, null);
        QueryResult qr = queryEngine.execute(es, 0);
        ResultSet rs = qr.unwrap(ResultSet.class);
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println("id=" + id + ", name=" + name);
        }
        qr.close();

//        queryEngine.executeQuery("SELECT ID FROM SQLITE.__DEFAULT.BITNINE WHERE id IN (SELECT id FROM SQLITE.__DEFAULT.BITNINE)");

        mc.close();
        schemaManager.stop();
        metaStoreService.stop();
    }
}
