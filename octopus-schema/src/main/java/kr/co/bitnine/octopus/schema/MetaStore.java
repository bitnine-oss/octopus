package kr.co.bitnine.octopus.schema;


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

    private HashMap<String, Database> tables = new HashMap<String, Table>();
    private HashMap<String, Table> tables = new HashMap<String, Table>();

}
