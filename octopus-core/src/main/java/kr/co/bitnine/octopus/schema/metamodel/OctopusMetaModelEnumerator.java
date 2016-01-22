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
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class OctopusMetaModelEnumerator implements Enumerator<Object> {
    private static final Log LOG = LogFactory.getLog(OctopusMetaModelEnumerator.class);

    private DataSet dataSet;
    private Row row;

    private String metaModelType;
    private OctopusMetaModelTableScan octopusMetaModelTableScan;

    public OctopusMetaModelEnumerator(OctopusMetaModelDataSource omd, String tableName,
                                      OctopusMetaModelTableScan octopusMetaModelTableScan) {
        JSONObject jsonObject = omd.getConnectionInfo();
        metaModelType = jsonObject.get("type").toString().toLowerCase();
        this.octopusMetaModelTableScan = octopusMetaModelTableScan;

        DataContext dc;

        switch (metaModelType) {
        case "elasticsearch" :
            dc = OctopusMetaModelDataContextFactory.createElasticSearchDataContext(jsonObject);
            break;
        case "mongodb" :
            dc = OctopusMetaModelDataContextFactory.createMongoDbDataContext(jsonObject);
            break;
        case "couchdb" :
            dc = OctopusMetaModelDataContextFactory.createCouchDbDataContext(jsonObject);
            break;
        case "cassandra" :
            dc = OctopusMetaModelDataContextFactory.createCassandraDataContext(jsonObject);
            break;
        default:
            dc = null;
            break;
        }
        this.dataSet = dc.query().from(tableName).select(getFieldString()).execute();

        LOG.debug("OctopusMetaModelEnumerator Scan. Metamodeltype:" + metaModelType);
    }

    public String[] getFieldString() {
        final List<String> fieldList = octopusMetaModelTableScan.getRowType().getFieldNames();
        String[] fieldString = new String[fieldList.size()];
        for (int i = 0; i < fieldList.size(); i++)
            fieldString[i] = fieldList.get(i);

        return fieldString;
    }

    @Override
    public Object current() {
        /*
          Currently, metamodel(version 4.5) has a problem for Elasticsearch that
          Metamodel recognizes a column as Long type, but the returned row is Integer.
          So, we explicitly convert Integer column into Long column for Elasticsearch results.
          (We assume that all integer values are Long type not Integer type in ES)
          */

        ArrayList<Object> resultRow;
        if ("elasticsearch".equals(metaModelType)) {
            resultRow = new ArrayList<Object>();
            for (Object column : row.getValues()) {
                if (column instanceof Integer)
                    resultRow.add(new Long(((Integer) column).longValue()));
                else
                    resultRow.add(column);
            }
            return resultRow.toArray();
        }

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
