package kr.co.bitnine.octopus.queryengine;

import kr.co.bitnine.octopus.schema.MetaStore;
import kr.co.bitnine.octopus.schema.model.MTable;
import org.apache.calcite.sql.*;

import java.util.ArrayList;
import java.util.List;

public class TableNameTranslator {

    MetaStore metaStore;

    public TableNameTranslator(MetaStore metastore) {
       this.metaStore = metastore;
    }

    /* translate FQN to DSN (datasource name) */
    public void toDSN(SqlNode query) {
        ArrayList<SqlIdentifier> tableIDs = new ArrayList<SqlIdentifier>();
        SqlNode accept = query.accept(new SqlTableIdentifierFindVisitor(tableIDs));

        for (SqlIdentifier tableID : tableIDs) {
            List<String> newName = new ArrayList<String>();
            newName.add(tableID.names.get(2));
            tableID.setNames(newName, null);
        }
    }

    /* translate DSN (datasource name) to FQN */
    public void toFQN(SqlNode query) {
        ArrayList<SqlIdentifier> tableIDs = new ArrayList<SqlIdentifier>();
        SqlNode accept = query.accept(new SqlTableIdentifierFindVisitor(tableIDs));

        for (SqlIdentifier tableID : tableIDs) {
            MTable mtable = metaStore.getTable(tableID);
            List<String> newName = new ArrayList<String>();
            newName.add(mtable.getSchema().getDatasource().getName());
            newName.add(mtable.getSchema().getName());
            newName.add(mtable.getName());
            tableID.setNames(newName, null);
        }
    }
}
