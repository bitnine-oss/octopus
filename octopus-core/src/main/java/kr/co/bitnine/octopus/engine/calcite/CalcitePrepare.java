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

package kr.co.bitnine.octopus.engine.calcite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.avatica.AvaticaParameter;
import org.apache.calcite.avatica.ColumnMetaData;
import org.apache.calcite.avatica.Meta;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.EnumerableDefaults;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.function.Function0;
import org.apache.calcite.linq4j.tree.ClassDeclaration;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.prepare.CalcitePrepareImpl;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.runtime.ArrayBindable;
import org.apache.calcite.runtime.Bindable;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.util.ImmutableIntList;

/**
 * API for a service that prepares statements for execution.
 */
public interface CalcitePrepare {
    Function0<org.apache.calcite.jdbc.CalcitePrepare> DEFAULT_FACTORY =
            new Function0<org.apache.calcite.jdbc.CalcitePrepare>() {
                public org.apache.calcite.jdbc.CalcitePrepare apply() {
                    return new CalcitePrepareImpl();
                }
            };
    ThreadLocal<ArrayList<Context>> THREAD_CONTEXT_STACK =
            new ThreadLocal<ArrayList<Context>>() {
                @Override
                protected ArrayList<Context> initialValue() {
                    return new ArrayList<>();
                }
            };

    ParseResult parse(Context context, String sql);

    ConvertResult convert(Context context, String sql);

    /**
     * Executes a DDL statement.
     * <p/>
     * <p>The statement identified itself as DDL in the
     * {@link org.apache.calcite.jdbc.CalcitePrepare.ParseResult#kind} field.
     */
    void executeDdl(Context context, SqlNode node);

    /**
     * Analyzes a view.
     *
     * @param context Context
     * @param sql     View SQL
     * @param fail    Whether to fail (and throw a descriptive error message) if the
     *                view is not modifiable
     * @return Result of analyzing the view
     */
    AnalyzeViewResult analyzeView(Context context, String sql, boolean fail);

    <T> CalciteSignature<T> prepareSql(
            Context context,
            Query<T> query,
            Type elementType,
            long maxRowCount);

    <T> CalciteSignature<T> prepareQueryable(
            Context context,
            Queryable<T> queryable);

    /**
     * Context for preparing a statement.
     */
    interface Context {
        JavaTypeFactory getTypeFactory();

        CalciteSchema getRootSchema();

        List<String> getDefaultSchemaPath();

        CalciteConnectionConfig config();

        /**
         * Returns the spark handler. Never null.
         */
        SparkHandler spark();

        DataContext getDataContext();
    }

    /**
     * Callback to register Spark as the main engine.
     */
    interface SparkHandler {
        RelNode flattenTypes(RelOptPlanner planner, RelNode rootRel,
                             boolean restructure);

        void registerRules(RuleSetBuilder builder);

        boolean enabled();

        ArrayBindable compile(ClassDeclaration expr, String s);

        Object sparkContext();

        /**
         * Allows Spark to declare the rules it needs.
         */
        interface RuleSetBuilder {
            void addRule(RelOptRule rule);

            void removeRule(RelOptRule rule);
        }
    }

    /**
     * The result of parsing and validating a SQL query.
     */
    class ParseResult {
        private final CalcitePrepareImpl prepare;
        private final String sql; // for debug
        private final SqlNode sqlNode;
        private final RelDataType rowType;
        private final RelDataTypeFactory typeFactory;

        ParseResult(CalcitePrepareImpl prepare, SqlValidator validator,
                    String sql,
                    SqlNode sqlNode, RelDataType rowType) {
            super();
            this.prepare = prepare;
            this.sql = sql;
            this.sqlNode = sqlNode;
            this.rowType = rowType;
            this.typeFactory = validator.getTypeFactory();
        }

        /**
         * Returns the kind of statement.
         * <p/>
         * <p>Possibilities include:
         * <p/>
         * <ul>
         * <li>Queries: usually {@link SqlKind#SELECT}, but
         * other query operators such as {@link SqlKind#UNION} and
         * {@link SqlKind#ORDER_BY} are possible
         * <li>DML statements: {@link SqlKind#INSERT}, {@link SqlKind#UPDATE} etc.
         * <li>Session control statements: {@link SqlKind#COMMIT}
         * <li>DDL statements: {@link SqlKind#CREATE_TABLE},
         * {@link SqlKind#DROP_INDEX}
         * </ul>
         *
         * @return Kind of statement, never null
         */
        public SqlKind kind() {
            return sqlNode.getKind();
        }
    }

    /**
     * The result of parsing and validating a SQL query and converting it to
     * relational algebra.
     */
    class ConvertResult extends ParseResult {
        private final RelRoot root;

        ConvertResult(CalcitePrepareImpl prepare, SqlValidator validator,
                      String sql, SqlNode sqlNode, RelDataType rowType, RelRoot root) {
            super(prepare, validator, sql, sqlNode, rowType);
            this.root = root;
        }
    }

    /**
     * The result of analyzing a view.
     */
    class AnalyzeViewResult extends ConvertResult {
        /**
         * Not null if and only if the view is modifiable.
         */
        private final Table table;
        private final ImmutableList<String> tablePath;
        private final RexNode constraint;
        private final ImmutableIntList columnMapping;

        AnalyzeViewResult(CalcitePrepareImpl prepare,
                          SqlValidator validator, String sql, SqlNode sqlNode,
                          RelDataType rowType, RelRoot root, Table table,
                          ImmutableList<String> tablePath, RexNode constraint,
                          ImmutableIntList columnMapping) {
            super(prepare, validator, sql, sqlNode, rowType, root);
            this.table = table;
            this.tablePath = tablePath;
            this.constraint = constraint;
            this.columnMapping = columnMapping;
        }
    }

    /**
     * The result of preparing a query. It gives the Avatica driver framework
     * the information it needs to create a prepared statement, or to execute a
     * statement directly, without an explicit prepare step.
     */
    class CalciteSignature<T> extends Meta.Signature {
        @JsonIgnore
        private final RelDataType rowType;
        @JsonIgnore
        private final List<RelCollation> collationList;
        private final long maxRowCount;
        private final Bindable<T> bindable;

        CalciteSignature(String sql, List<AvaticaParameter> parameterList,
                         Map<String, Object> internalParameters, RelDataType rowType,
                         List<ColumnMetaData> columns, Meta.CursorFactory cursorFactory,
                         List<RelCollation> collationList, long maxRowCount,
                         Bindable<T> bindable) {
            super(columns, sql, parameterList, internalParameters, cursorFactory, null);
            this.rowType = rowType;
            this.collationList = collationList;
            this.maxRowCount = maxRowCount;
            this.bindable = bindable;
        }

        CalciteSignature(String sql,
                         List<AvaticaParameter> parameterList,
                         Map<String, Object> internalParameters,
                         RelDataType rowType,
                         List<ColumnMetaData> columns,
                         Meta.CursorFactory cursorFactory,
                         List<RelCollation> collationList,
                         long maxRowCount,
                         Bindable<T> bindable,
                         Meta.StatementType statementType) {
            super(columns, sql, parameterList, internalParameters, cursorFactory,
                    statementType);
            this.rowType = rowType;
            this.collationList = collationList;
            this.maxRowCount = maxRowCount;
            this.bindable = bindable;
        }

        public Enumerable<T> enumerable(DataContext dataContext) {
            Enumerable<T> enumerable = bindable.bind(dataContext);
            if (maxRowCount >= 0) {
                // Apply limit. In JDBC 0 means "no limit". But for us, -1 means
                // "no limit", and 0 is a valid limit.
                enumerable = EnumerableDefaults.take(enumerable, maxRowCount);
            }
            return enumerable;
        }

        public List<RelCollation> getCollationList() {
            return collationList;
        }
    }

    /**
     * A union type of the three possible ways of expressing a query: as a SQL
     * string, a {@link Queryable} or a {@link RelNode}. Exactly one must be
     * provided.
     */
    //CHECKSTYLE:OFF
    final class Query<T> {
    //CHECKSTYLE:ON
        private final String sql;
        private final Queryable<T> queryable;
        private final RelNode rel;

        private Query(String sql, Queryable<T> queryable, RelNode rel) {
            this.sql = sql;
            this.queryable = queryable;
            this.rel = rel;

            assert (sql == null ? 0 : 1)
                    + (queryable == null ? 0 : 1)
                    + (rel == null ? 0 : 1) == 1;
        }

        public static <T> Query<T> of(String sql) {
            return new Query<>(sql, null, null);
        }

        public static <T> Query<T> of(Queryable<T> queryable) {
            return new Query<>(null, queryable, null);
        }

        public static <T> Query<T> of(RelNode rel) {
            return new Query<>(null, null, rel);
        }
    }
}
