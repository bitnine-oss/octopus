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
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.schema.Schema;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

public class Database
{
    private final DataContext dataContext;

    public Database(Connection connection)
    {
        DataContext dc = DataContextFactory.createJdbcDataContext(connection);
        this.dataContext = dc;
    }

    private org.apache.calcite.schema.Schema schema = null;

    public org.apache.calcite.schema.Schema getSchema()
    {
        if (schema == null) {
            schema = new org.apache.calcite.schema.impl.AbstractSchema() {
                private final ImmutableMap<String, org.apache.calcite.schema.Schema> subSchemaMap;

                {
                    ImmutableMap.Builder<String, org.apache.calcite.schema.Schema> builder = ImmutableMap.builder();
                    for (OctopusSchema schema : getSchemas()) {
                        String name = schema.getName();
                        if (name == null)
                            name = "__DEFAULT"; // FIXME
                        builder.put(name, schema);
                    }
                    subSchemaMap = builder.build();
                }

                @Override
                public boolean isMutable()
                {
                    return false;
                }

                @Override
                protected Map<String, org.apache.calcite.schema.Schema> getSubSchemaMap()
                {
                    return subSchemaMap;
                }
            };
        }

        return schema;
    }

    public OctopusSchema[] getSchemas()
    {
        ArrayList<OctopusSchema> schemas = new ArrayList();
        for (Schema schema : dataContext.getSchemas())
            schemas.add(new OctopusSchema(schema));

        return schemas.toArray(new OctopusSchema[schemas.size()]);
    }
}
