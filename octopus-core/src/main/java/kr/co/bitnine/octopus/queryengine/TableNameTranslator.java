package kr.co.bitnine.octopus.queryengine;

import kr.co.bitnine.octopus.schema.MetaStore;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.metamodel.schema.Table;

import java.util.List;
import java.util.Stack;

public class TableNameTranslator {

    MetaStore metaStore;

    public TableNameTranslator(MetaStore metastore) {
       this.metaStore = metastore;
    }

    /* translate FQN to DSN (datasource name) */
    public void toDSN(SqlNode query) {
        SqlNode accept = query.accept(new SqlTableIdentifierFindVisitor());
    }

    public void toFQN(SqlNode query) {
        //SqlNode accept = query.accept( );
    }

}
