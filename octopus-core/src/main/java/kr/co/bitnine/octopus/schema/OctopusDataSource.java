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
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Map;

public final class OctopusDataSource extends AbstractSchema {
    private final String name;
    private final ImmutableMap<String, Schema> subSchemaMap;

    public OctopusDataSource(MetaDataSource metaDataSource) {
        name = metaDataSource.getName();

        ImmutableMap.Builder<String, Schema> builder = ImmutableMap.builder();
        for (MetaSchema metaSchema : metaDataSource.getSchemas())
            builder.put(metaSchema.getName(), new OctopusSchema(metaSchema, this));
        subSchemaMap = builder.build();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    protected Map<String, Schema> getSubSchemaMap() {
        return subSchemaMap;
    }
}
