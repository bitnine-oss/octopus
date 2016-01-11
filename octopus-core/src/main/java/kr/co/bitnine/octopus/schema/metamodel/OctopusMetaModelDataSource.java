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

package kr.co.bitnine.octopus.schema.metamodel;

import com.google.common.collect.ImmutableMap;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import kr.co.bitnine.octopus.schema.OctopusDataSource;
import org.apache.calcite.schema.Schema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class OctopusMetaModelDataSource extends OctopusDataSource {
    private static final Log LOG = LogFactory.getLog(OctopusMetaModelDataSource.class);

    private JSONObject connectionInfo;

    public OctopusMetaModelDataSource(MetaDataSource metaDataSource) {
        super(metaDataSource);

        LOG.debug("create OctopusMetaModelDataSource. dataSourceName: " + metaDataSource.getName());

        JSONParser jsonParser = new JSONParser();
        try {
            connectionInfo = (JSONObject) jsonParser.parse(metaDataSource.getConnectionString());
        } catch (ParseException ignore) {
            /*
             * NOTE: parse() never fail, because ADD DATASOURCE... succeeded.
             *       This assumption is NOT preferable.
             */
        }

        ImmutableMap.Builder<String, Schema> builder = ImmutableMap.builder();
        for (MetaSchema metaSchema : metaDataSource.getSchemas())
            builder.put(metaSchema.getName(), new OctopusMetaModelSchema(metaSchema, this));
        setSubSchemaMap(builder.build());
    }

    public JSONObject getConnectionInfo() {
        return connectionInfo;
    }
}
