package kr.co.bitnine.octopus.queryengine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableListIterator;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.util.SqlBasicVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SqlTableIdentifierFindVisitor extends SqlBasicVisitor<SqlNode> {
    /* to find table name Identifier, we use a state stack.
       It is used to indicate whether an identifier is in FROM clause
     */
    private enum State {
        NOT_FROM,
        FROM
    }

    private final Stack<State> nodeStack = new Stack<State>();
    private ArrayList<SqlIdentifier> tableIDs;

    public SqlTableIdentifierFindVisitor(ArrayList<SqlIdentifier> tableIDs) {
        this.tableIDs = tableIDs;
    }

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
            tableIDs.add(identifier);
            //System.out.println("Table Name: " + identifier.toString());
            //System.out.println("To Table Name: " + identifier.toString());
        }
        return identifier;
    }
}
