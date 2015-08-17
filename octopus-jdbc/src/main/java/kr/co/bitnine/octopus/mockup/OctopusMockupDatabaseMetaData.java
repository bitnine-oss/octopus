package kr.co.bitnine.octopus.mockup;

import org.postgresql.core.PGStream;
import org.postgresql.jdbc2.AbstractJdbc2Statement;
import org.postgresql.jdbc4.AbstractJdbc4DatabaseMetaData;
import org.postgresql.jdbc4.Jdbc4Connection;
import org.postgresql.jdbc4.Jdbc4ResultSet;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.*;

public class OctopusMockupDatabaseMetaData implements DatabaseMetaData {
    private static final char SEARCH_STRING_ESCAPE = '\\';

    class Datasource {
        String datasource_name;
        String comment;
        public List<Schema> schemas;

        public Datasource (String datasource_name , String comment, List<Schema> schemas) {
            this.datasource_name = datasource_name;
            this.comment = comment;
            this.schemas = schemas;
        }

    };

    class Schema {
        String schema_name;
        String comment;
        public List<Table> tables;

        public Schema (String schema_name, String comment, List<Table> tables) {
            this.schema_name = schema_name;
            this.comment = comment;
            this.tables = tables;
        }
    };

    class Table {
        String table_name;
        String type;
        String comment;
        public List<Column> columns;

        public Table (String table_name, String type, String comment, List<Column> columns) {
            this.table_name = table_name;
            this.type = type;
            this.comment = comment;
            this.columns = columns;
        }
    };

    class Column {
        String column_name;
        String type;
        String secu_type;
        public String comment;

        public Column (String column_name, String type, String secu_type, String comment) {
            this.column_name = column_name;
            this.type = type;
            this.comment = comment;
            this.secu_type = secu_type;
        }
    }

    static List<Datasource> meta_info;

    public OctopusMockupDatabaseMetaData() {
        meta_info = Arrays.asList(
                new Datasource("Datasource 1", "Oracle 1", Arrays.asList(
                        new Schema("Schema 1", "schema 1", Arrays.asList(
                                new Table("Table 1", "TABLE", "table 1", Arrays.asList(
                                        new Column("Column 1", "VARCHAR(200)", "Confidential", "column 1"),
                                        new Column("Column 2", "VARCHAR(200)", "Public", "column 2"),
                                        new Column("Column 3", "VARCHAR(200)", "Private", "column 3")
                                        ))
                        ))
                )),
                new Datasource("Datasource 2", "Oracle 2", Arrays.asList(
                        new Schema("Schema 2", "schema 2", Arrays.asList(
                                new Table("Table 1", "TABLE", "table 1", Arrays.asList(
                                        new Column("Column 1", "VARCHAR(200)", "Confidential", "column 1"),
                                        new Column("Column 2", "VARCHAR(200)", "Public", "column 2"),
                                        new Column("Column 3", "VARCHAR(200)", "Private", "column 3")
                                        ))
                        ))
                ))
        );
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return false;
    }

    @Override
    public String getURL() throws SQLException {
        return null;
    }

    @Override
    public String getUserName() throws SQLException {
        return null;
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }

    @Override
    public String getDatabaseProductName() throws SQLException {
        return null;
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return null;
    }

    @Override
    public String getDriverName() throws SQLException {
        return null;
    }

    @Override
    public String getDriverVersion() throws SQLException {
        return null;
    }

    @Override
    public int getDriverMajorVersion() {
        return 0;
    }

    @Override
    public int getDriverMinorVersion() {
        return 0;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException {
        return false;
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException {
        return null;
    }

    @Override
    public String getSQLKeywords() throws SQLException {
        return null;
    }

    @Override
    public String getNumericFunctions() throws SQLException {
        return null;
    }

    @Override
    public String getStringFunctions() throws SQLException {
        return null;
    }

    @Override
    public String getSystemFunctions() throws SQLException {
        return null;
    }

    @Override
    public String getTimeDateFunctions() throws SQLException {
        return null;
    }

    @Override
    public String getSearchStringEscape() throws SQLException {
        return null;
    }

    @Override
    public String getExtraNameCharacters() throws SQLException {
        return null;
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsConvert(int i, int i1) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return false;
    }

    @Override
    public String getSchemaTerm() throws SQLException {
        return null;
    }

    @Override
    public String getProcedureTerm() throws SQLException {
        return null;
    }

    @Override
    public String getCatalogTerm() throws SQLException {
        return null;
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return false;
    }

    @Override
    public String getCatalogSeparator() throws SQLException {
        return null;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsUnion() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxConnections() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxIndexLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxRowSize() throws SQLException {
        return 0;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return false;
    }

    @Override
    public int getMaxStatementLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxStatements() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 0;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 0;
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        return 0;
    }

    @Override
    public boolean supportsTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getProcedures(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getProcedureColumns(String s, String s1, String s2, String s3) throws SQLException {
        return null;
    }

    /**
     * Convert a pattern containing JDBC catalog search wildcards into
     * Java regex patterns.
     *
     * @param pattern input which may contain '%' or '_' wildcard characters, or
     * these characters escaped using {@link #getSearchStringEscape()}.
     * @return replace %/_ with regex search characters, also handle escaped
     * characters.
     *
     * From tajo code
     */
    private String convertPattern(final String pattern) {
        if (pattern == null) {
            return ".*";
        } else {
            StringBuilder result = new StringBuilder(pattern.length());

            boolean escaped = false;
            for (int i = 0, len = pattern.length(); i < len; i++) {
                char c = pattern.charAt(i);
                if (escaped) {
                    if (c != SEARCH_STRING_ESCAPE) {
                        escaped = false;
                    }
                    result.append(c);
                } else {
                    if (c == SEARCH_STRING_ESCAPE) {
                        escaped = true;
                        continue;
                    } else if (c == '%') {
                        result.append(".*");
                    } else if (c == '_') {
                        result.append('.');
                    } else {
                        result.append(c);
                    }
                }
            }

            return result.toString();
        }
    }

    public java.sql.ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) throws SQLException
    {
        OctopusMockupResultSet resultSet = new OctopusMockupResultSet();
        String regTableNamePattern = convertPattern(tableNamePattern);
        String regSchemaNamePattern = convertPattern(schemaPattern);

        for (Datasource datasource: meta_info) {
            if (catalog != null && !catalog.equals(datasource.datasource_name))
                continue;

            String catalog_name = datasource.datasource_name;

            for (Schema schema : datasource.schemas) {
                if (regSchemaNamePattern != null && !schema.schema_name.matches(regSchemaNamePattern))
                    continue;

                String schema_name = schema.schema_name;

                for (Table table : schema.tables) {
                    if (regTableNamePattern != null && !table.table_name.matches(regTableNamePattern))
                        continue;

                    String table_name = table.table_name;

                    resultSet.addTuple(Arrays.asList(catalog_name, schema_name, table_name, table.type, table.comment));
                }
            }
        }
        return resultSet;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        OctopusMockupResultSet resultSet = new OctopusMockupResultSet();

        for (Datasource datasource : meta_info) {
            for (Schema schema : datasource.schemas) {
                List<String> tuple = new ArrayList<String>();
                tuple.add(datasource.datasource_name);
                tuple.add(schema.schema_name);
                tuple.add(schema.comment);
                resultSet.addTuple(tuple);
            }
        }
        return resultSet;
    }

    @Override
    public ResultSet getCatalogs() throws SQLException {
        OctopusMockupResultSet resultSet = new OctopusMockupResultSet();

        for (Datasource datasource : meta_info) {
            List<String> tuple = new ArrayList<String>();
            tuple.add(datasource.datasource_name);
            tuple.add(datasource.comment);
            resultSet.addTuple(tuple);
        }
        return resultSet;
    }

    @Override
    public ResultSet getTableTypes() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        OctopusMockupResultSet resultSet = new OctopusMockupResultSet();
        String regSchemaNamePattern = convertPattern(schemaPattern);
        String regTableNamePattern = convertPattern(tableNamePattern);
        String regColumnNamePattern = convertPattern(columnNamePattern);

        for (Datasource datasource: meta_info) {
            if (catalog != null && !catalog.equals(datasource.datasource_name))
                continue;

            String catalog_name = datasource.datasource_name;

            for (Schema schema : datasource.schemas) {
                if (regSchemaNamePattern != null && !schema.schema_name.matches(regSchemaNamePattern))
                    continue;

                String schema_name = schema.schema_name;

                for (Table table : schema.tables) {
                    if (regTableNamePattern != null && !table.table_name.matches(regTableNamePattern))
                        continue;

                    String table_name = table.table_name;

                    for (Column column : table.columns) {
                        if (regColumnNamePattern != null && !column.column_name.matches(regColumnNamePattern))
                            continue;

                        resultSet.addTuple(Arrays.asList(catalog_name, schema_name, table_name, column.column_name, column.type, column.secu_type, column.comment));
                    }
                }
            }
        }
        return resultSet;
    }

    @Override
    public ResultSet getColumnPrivileges(String s, String s1, String s2, String s3) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTablePrivileges(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getBestRowIdentifier(String s, String s1, String s2, int i, boolean b) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getVersionColumns(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getPrimaryKeys(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getImportedKeys(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getExportedKeys(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getCrossReference(String s, String s1, String s2, String s3, String s4, String s5) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getIndexInfo(String s, String s1, String s2, boolean b, boolean b1) throws SQLException {
        return null;
    }

    @Override
    public boolean supportsResultSetType(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int i, int i1) throws SQLException {
        return false;
    }

    @Override
    public boolean ownUpdatesAreVisible(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int i) throws SQLException {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getUDTs(String s, String s1, String s2, int[] ints) throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getSuperTypes(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getSuperTables(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getAttributes(String s, String s1, String s2, String s3) throws SQLException {
        return null;
    }

    @Override
    public boolean supportsResultSetHoldability(int i) throws SQLException {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }

    @Override
    public int getSQLStateType() throws SQLException {
        return 0;
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        OctopusMockupResultSet resultSet = new OctopusMockupResultSet();
        String regSchemaNamePattern = convertPattern(schemaPattern);

        for (Datasource datasource: meta_info) {
            if (catalog != null && !catalog.equals(datasource.datasource_name))
                continue;

            String catalog_name = datasource.datasource_name;

            for (Schema schema : datasource.schemas) {
                if (regSchemaNamePattern != null && !schema.schema_name.matches(regSchemaNamePattern))
                    continue;

                String schema_name = schema.schema_name;

                List<String> tuple = new ArrayList<String>();
                tuple.add(datasource.datasource_name);
                tuple.add(schema.schema_name);
                tuple.add(schema.comment);
                resultSet.addTuple(tuple);
            }
        }
        return resultSet;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctions(String s, String s1, String s2) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getFunctionColumns(String s, String s1, String s2, String s3) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getPseudoColumns(String s, String s1, String s2, String s3) throws SQLException {
        return null;
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }
}
