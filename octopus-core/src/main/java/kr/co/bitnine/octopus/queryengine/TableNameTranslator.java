package kr.co.bitnine.octopus.queryengine;

import kr.co.bitnine.octopus.schema.MetaStore;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.metamodel.schema.Table;

import java.util.List;
import java.util.Stack;

public class TableNameTranslator {
    /* to find table name Identifier, we use a state stack.
       It is used to indicate whether an identifier is in FROM clause
     */
    private enum State {
        NOT_FROM,
        FROM
    }


    MetaStore metaStore;

    public TableNameTranslator(MetaStore metastore) {
       this.metaStore = metastore;
    }

    public void toFQN(SqlNode query) {
        SqlNode accept = query.accept(
                new SqlBasicVisitor<SqlNode>() {
                    private final Stack<State> nodeStack = new Stack<State>();

                    @Override
                    public SqlNode visit(SqlCall call) {
                        if (call instanceof SqlSelect) {
                            int i = 0;
                            for (SqlNode operand : call.getOperandList()) {
                                // From operand
                                if (i == 2)
                                    nodeStack.push(State.FROM);
                                else
                                    nodeStack.push(State.NOT_FROM);

                                i++;
                                if (operand == null) {
                                    continue;
                                }
                                operand.accept(this);
                                nodeStack.pop();
                            }
                            return null;
                        }
                        SqlOperator operator = call.getOperator();
                        if (operator instanceof SqlOperator && operator.getKind() == SqlKind.AS) {
                                /* AS operator will be probed only if it is in FROM clause */
                            if (nodeStack.peek() == State.FROM)
                                call.operand(0).accept(this);
                            return null;
                        }

                        return super.visit(call);
                    }

                    @Override
                    public SqlNode visit(SqlIdentifier identifier) {
                        // check whether this is fully qualified table name
                        if (nodeStack.peek() == State.FROM) {
                            System.out.println("Table Name: " + identifier.toString());
                        }
                        return identifier;
                    }
                }
        );
    }

}
