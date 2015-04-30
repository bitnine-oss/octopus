package kr.co.bitnine.octopus.queryengine;

import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.ValidationException;

/**
 * Created by kisung on 15. 4. 28.
 */
public class QueryEngine {
    public void executeQuery (String sql) throws Exception {
        final SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        final FrameworkConfig config = Frameworks.newConfigBuilder()
                .defaultSchema(
                        CalciteAssert.addSchema(rootSchema, CalciteAssert.SchemaSpec.HR))
                .build();
        final Planner planner = Frameworks.getPlanner(config);
        SqlNode parse = planner.parse(sql);
        SqlNode val = planner.validate(parse);

        String valStr =
                val.toSqlString(SqlDialect.DUMMY, false).getSql();

        System.out.println(valStr);
    }
}
