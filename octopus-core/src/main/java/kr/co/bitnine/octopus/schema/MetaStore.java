package kr.co.bitnine.octopus.schema;


import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Schema;

import java.util.HashMap;

/**
 * In-memory meta-store
 *
 * Octopus metastore has schema information; tables, columns and views.
 * It should support fast search on the schema information.
 *
 * Created by kisung on 15. 4. 21.
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
