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

package kr.co.bitnine.octopus.meta.logs;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public final class UpdateLoggerImpl implements UpdateLogger {
    private final String dataSourceName;
    private final PrintWriter writer;

    private String defaultSchemaName;

    public UpdateLoggerImpl(String filePath, String dataSourceName)
            throws FileNotFoundException, UnsupportedEncodingException {

        this.dataSourceName = dataSourceName;
        this.writer = new PrintWriter(filePath, "UTF-8");
    }

    public void begin() {
        writer.println("BEGIN " + dataSourceName);
    }

    @Override
    public void setDefaultSchema(String schemaName) {
        this.defaultSchemaName = schemaName;
    }

    @Override
    public void create(String schemaName) {
        writer.println("CREATE SCHEMA \"" + schemaName + '"');
    }

    @Override
    public void create(String schemaName, String tableName) {
        String s = schemaName == null ? defaultSchemaName : schemaName;
        writer.println("CREATE TABLE \"" + s + "\".\"" + tableName + '"');
    }

    @Override
    public void delete(String schemaName) {
        writer.println("DELETE SCHEMA \"" + schemaName + '"');
    }

    @Override
    public void delete(String schemaName, String tableName) {
        String s = schemaName == null ? defaultSchemaName : schemaName;
        writer.println("DELETE TABLE \"" + s + "\".\"" + tableName + '"');
    }

    @Override
    public void end() {
        writer.println("END " + dataSourceName);
    }

    @Override
    public void close() {
        writer.close();
    }
}
