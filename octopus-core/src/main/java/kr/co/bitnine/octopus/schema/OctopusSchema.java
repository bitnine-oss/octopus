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

package kr.co.bitnine.octopus.schema;

import com.google.common.collect.ImmutableMap;
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class OctopusSchema extends AbstractSchema {
    private static final Log LOG = LogFactory.getLog(OctopusSchema.class);

    private final String name;
    private final OctopusDataSource dataSource;
    private ImmutableMap<String, Table> tableMap;

    public OctopusSchema(MetaSchema metaSchema, OctopusDataSource dataSource) {
        name = metaSchema.getName();
        this.dataSource = dataSource;
    }

    public final String getName() {
        return name;
    }

    @Override
    public final boolean isMutable() {
        return false;
    }

    @Override
    public final Map<String, Table> getTableMap() {
        LOG.debug("OctopusSchema getTableMap called. tableMapSize: " + tableMap.size());
        return tableMap;
    }

    public final void setTableMap(ImmutableMap<String, Table> tableMap) {
        this.tableMap = tableMap;
    }

    public final OctopusDataSource getDataSource() {
        return dataSource;
    }
}
