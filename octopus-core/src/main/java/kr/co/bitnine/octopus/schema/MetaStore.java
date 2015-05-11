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

import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Schema;

import java.util.HashMap;

/**
 * In-memory meta-store
 *
 * Octopus metastore has schema information; tables, columns and views.
 * It should support fast search on the schema information.
 */
public class MetaStore {

    private HashMap<String, Database> databases = new HashMap<String, Database>();
    private HashMap<String, Table> tables = new HashMap<String, Table>();

    /* JDBC-type database */
    public void insertDatabase(String name, String type,
                                   Database.DBConnInfo dbconn, DataContext dc) {
        /* NOTE:
           Actually we should get all schema which the user can access.
           However, current MetaStore has a bug that stalls when getting
           all schemas. So now we get only the default schema here.
         */
        Schema schema = dc.getDefaultSchema();

        Database db = new Database(name, type, dbconn);

        for (org.apache.metamodel.schema.Table tbl : schema.getTables()) {
            Table table = new Table(tbl);
            db.insertTable(tbl.getName(), table);
            tables.put(tbl.getName(), table);
        }
        databases.put(name, db);
    }

    /* for test */
    public void insertDatabase(String name, Database db) {
        databases.put(name, db);
        for (Table table : db.getTables()) {
            tables.put(table.getName(), table);
        }
    }
}
