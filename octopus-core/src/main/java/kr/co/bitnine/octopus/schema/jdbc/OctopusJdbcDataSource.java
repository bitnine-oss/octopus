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

package kr.co.bitnine.octopus.schema.jdbc;

import com.google.common.collect.ImmutableMap;
import javax.sql.DataSource;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import kr.co.bitnine.octopus.schema.OctopusDataSource;
import org.apache.calcite.adapter.jdbc.JdbcConvention;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.sql.SqlDialect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OctopusJdbcDataSource extends OctopusDataSource {
    private static final Log LOG = LogFactory.getLog(OctopusJdbcDataSource.class);

    public final SqlDialect dialect;
    final JdbcConvention convention;
    final DataSource dataSource;

    public OctopusJdbcDataSource(SchemaPlus parentSchema, MetaDataSource metaDataSource) {
        super(metaDataSource);

        LOG.debug("create OctopusJdbcDataSource. dataSourceName: " + metaDataSource.getName());
        dataSource = dataSource(metaDataSource.getConnectionString(), metaDataSource.getDriverName());

        /* TODO: what is this? */
        final Expression expression =
                Schemas.subSchemaExpression(parentSchema, name, JdbcSchema.class);

        this.dialect = createDialect(dataSource);
        this.convention = JdbcConvention.of(dialect, expression, metaDataSource.getName());

        ImmutableMap.Builder<String, Schema> builder = ImmutableMap.builder();
        for (MetaSchema metaSchema : metaDataSource.getSchemas())
            builder.put(metaSchema.getName(), new OctopusJdbcSchema(metaSchema, this));
        subSchemaMap = builder.build();
    }

    /** Creates a JDBC data source with the given specification. */
    public static DataSource dataSource(String connectionString, String driverClassName) {
        return JdbcUtils.DataSourcePool.INSTANCE.get(connectionString, driverClassName);
    }

    /** Returns a suitable SQL dialect for the given data source. */
    public static SqlDialect createDialect(DataSource dataSource) {
        return JdbcUtils.DialectPool.INSTANCE.get(dataSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
