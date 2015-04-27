package kr.co.bitnine.octopus.schema;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableTable;

import java.util.HashMap;

/**
 * Created by kisung on 15. 4. 21.
 */
public class Table extends MutableTable {

    private HashMap<String, Column> _columns = new HashMap<String, Column>();
    protected String _name;

    public Table (org.apache.metamodel.schema.Table tbl) {
        _name = tbl.getName();

        for (Column col : tbl.getColumns()) {
            _columns.put(col.getName(), col);
        }
    }
}
