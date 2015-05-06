package kr.co.bitnine.octopus.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by kisung on 15. 4. 21.
 */
public class Database {

    static public class DBConnInfo {
        /* for JDBC connection */

        protected String _driver;
        protected String _connectionString;

        public DBConnInfo(String driver, String connectionString) {
            _driver = driver;
            _connectionString = connectionString;
        }
    }

    private HashMap<String, Table> _tables = new HashMap<String, Table>();

    protected String _name;
    protected String _type;

    /* Connection information */
    protected DBConnInfo _dbc;

    public Database (String name)
    {
        _name = name;
    }

    /* for JDBC connection */
    public Database (String name, String type, DBConnInfo dbc)
    {
        _name = name;
        _type = type;
        _dbc = dbc;
    }

    public void insertTable(Table tbl) {
        _tables.put(tbl.getName(), tbl);
    }

    public void insertTable(String name, Table tbl) {
        _tables.put(name, tbl);
    }

    public String getName() {
        return _name;
    }

    public Collection<Table> getTables() {
        return _tables.values();
    }
}
