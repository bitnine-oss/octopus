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
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.schema.OctopusDataSource;
import kr.co.bitnine.octopus.schema.OctopusSchema;
import org.apache.calcite.schema.Table;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OctopusJdbcSchema extends OctopusSchema {
    private static final Log LOG = LogFactory.getLog(OctopusJdbcSchema.class);

    public OctopusJdbcSchema(MetaSchema metaSchema, OctopusDataSource dataSource) {
        super(metaSchema, dataSource);

        LOG.debug("create OctopusJdbcSchema. schemaName: " + metaSchema.getName());

        ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
        for (MetaTable metaTable : metaSchema.getTables())
            builder.put(metaTable.getName(), new OctopusJdbcTable(metaTable, this));
        setTableMap(builder.build());
    }
}
