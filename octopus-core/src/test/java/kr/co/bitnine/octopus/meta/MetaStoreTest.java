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

package kr.co.bitnine.octopus.meta;

import kr.co.bitnine.octopus.TestDb;
import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.meta.model.MetaColumn;
import kr.co.bitnine.octopus.meta.model.MetaTable;
import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

public class MetaStoreTest
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
    public void testMetaStore() throws Exception
    {
        Configuration conf = new OctopusConfiguration();
        testDb.setMetaStoreConf(conf);

        MetaStore metaStore = MetaStores.newInstance(conf.get("metastore.class"));
        MetaStoreService metaStoreService = new MetaStoreService(metaStore);
        metaStoreService.init(conf);
        metaStoreService.start();

        MetaContext mc = metaStore.getMetaContext();
        mc.addJdbcDataSource(testDb.getDriverName(), testDb.getTestDbURL(), "SQLITE");

        MetaTable metaTable = mc.getTableByName("BITNINE");
        Collection<MetaColumn> columns = metaTable.getColumns();
        System.out.println("number of columns: " + columns.size());
        for (MetaColumn metaColumn : columns)
            System.out.println("columnName=" + metaColumn.getName());

        mc.close();

        metaStoreService.stop();
    }
}
