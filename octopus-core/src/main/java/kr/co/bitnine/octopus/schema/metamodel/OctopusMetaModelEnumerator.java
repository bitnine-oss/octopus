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

import org.apache.calcite.linq4j.Enumerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;

public final class OctopusMetaModelEnumerator implements Enumerator<Object> {
    private static final Log LOG = LogFactory.getLog(OctopusMetaModelEnumerator.class);

    private DataSet dataSet;
    private Row row;

    public OctopusMetaModelEnumerator(OctopusMetaModelDataSource omd, String tableName) {
        //JSONObject jsonObject = omd.getConnectionInfo();
        //this.dataSet = dataContext.query().from(tableName).selectAll().execute();

        LOG.debug("OctopusMetaModelEnumerator Scan");
    }

    @Override
    public Object current() {
        return row.getValues();
    }

    @Override
    public boolean moveNext() {
        try {
            if (dataSet.next()) {
                row = dataSet.getRow();
                return true;
            } else {
                dataSet = null;
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        if (dataSet instanceof DataSet) {
            dataSet.close();
        }
    }
}
