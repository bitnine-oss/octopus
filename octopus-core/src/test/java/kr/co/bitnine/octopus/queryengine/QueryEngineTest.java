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
import kr.co.bitnine.octopus.schema.Table;

import org.junit.Before;
import org.junit.Test;

import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.Frameworks;

public class QueryEngineTest {

    MetaStore metastore;

    @Before
    public void setUp() throws Exception
    {
        metastore = new MetaStore();
        Class.forName("org.postgresql.Driver");
        Database database = new Database("orcl1");
        Table tb1 = new Table("T1");
        Table tb2 = new Table("T2");
        Table tb3 = new Table("T3");
        Table tb4 = new Table("T4");
        database.insertTable(tb1);
        database.insertTable(tb2);
        database.insertTable(tb3);
        database.insertTable(tb4);
        metastore.insertDatabase("orcl1", database);
    }

    @Test
    public void testQueryParsing() throws Exception {
        final SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        final FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.Config.DEFAULT)
                .defaultSchema(rootSchema)
                .build();
        Planner planner = Frameworks.getPlanner(config);
        SqlNode parse = planner.parse("select * from T1, T2");
        System.out.println(parse.toString());
        if (parse.getKind() == SqlKind.SELECT) {
            System.out.println("select query");
            SqlSelect select = (SqlSelect) parse;
            for (SqlNode node : select.getSelectList()) {
                System.out.println(node.toString());
            }
//            System.out.println("from: " + select.getFrom());
        }
    }
}
