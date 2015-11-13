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

package kr.co.bitnine.octopus.meta;

public final class ResultOfGetColumns {
    private String dsName;
    private String schemaName;
    private String tableName;
    private String colName;
    private int colType;
    private String comment;
    private String dataCategory;
    private String dsComment;
    private String schemaComment;
    private String tableComment;

    public ResultOfGetColumns() {
        // this constructor with no args. is required for Datanuclues to return this-typed results
    }

    public String getDsName() {
        return dsName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColName() {
        return colName;
    }

    public int getColType() {
        return colType;
    }

    public String getComment() {
        return comment;
    }

    public String getDataCategory() {
        return dataCategory;
    }

    public String getDsComment() {
        return dsComment;
    }

    public String getSchemaComment() {
        return schemaComment;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setDsName(String dsName) {
        this.dsName = dsName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public void setColType(int colType) {
        this.colType = colType;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDataCategory(String dataCategory) {
        this.dataCategory = dataCategory;
    }

    public void setDsComment(String dsComment) {
        this.dsComment = dsComment;
    }

    public void setSchemaComment(String schemaComment) {
        this.schemaComment = schemaComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }
}

