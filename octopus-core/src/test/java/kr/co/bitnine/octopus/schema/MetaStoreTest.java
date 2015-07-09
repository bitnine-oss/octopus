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

package kr.co.bitnine.octopus.schema;

import kr.co.bitnine.octopus.TestDb;
import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.schema.model.MColumn;
import kr.co.bitnine.octopus.schema.model.MTable;
import org.apache.hadoop.conf.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;

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
    public void testMetastore() throws Exception
    {
        Configuration conf = new OctopusConfiguration();
        testDb.setMetaStoreConf(conf);

        MetaStore.init(conf);
        MetaStore metaStore = MetaStore.get();
        Connection conn = testDb.getTestDbConnection();
        metaStore.addDataSource("SQLITE", testDb.getDriverName(), testDb.getTestDbURL(), conn, "test database");
/*
        MTable mtable = metaStore.getTable("bitnine");
        System.out.println("column cnt: " + mtable.getColumnCnt());

        for (MColumn column : mtable.getColumns())
            System.out.println("Column: " + column.getName());
 */
        conn.close();
        metaStore.destroy();
    }
}
