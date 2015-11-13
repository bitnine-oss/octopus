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

package kr.co.bitnine.octopus.meta.result;

public final class ResultOfGetColumns {
    private String dataSourceName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private int columnType;
    private String comment;
    private String dataCategory;
    private String dataSourceComment;
    private String schemaComment;
    private String tableComment;

    /*
     * This constructor with no args. is required for DataNucleus
     * to return this-typed results
     */
    public ResultOfGetColumns() { }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getColumnType() {
        return columnType;
    }

    public String getComment() {
        return comment;
    }

    public String getDataCategory() {
        return dataCategory;
    }

    public String getDataSourceComment() {
        return dataSourceComment;
    }

    public String getSchemaComment() {
        return schemaComment;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDataCategory(String dataCategory) {
        this.dataCategory = dataCategory;
    }

    public void setDataSourceComment(String dataSourceComment) {
        this.dataSourceComment = dataSourceComment;
    }

    public void setSchemaComment(String schemaComment) {
        this.schemaComment = schemaComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }
}
