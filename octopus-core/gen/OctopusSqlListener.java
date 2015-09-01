// Generated from /home/kisung/octopus/octopus-core/src/main/antlr4/kr/co/bitnine/octopus/sql/OctopusSql.g4 by ANTLR 4.5.1

    import org.apache.commons.lang3.StringUtils;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link OctopusSqlParser}.
 */
public interface OctopusSqlListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#ddl}.
	 * @param ctx the parse tree
	 */
	void enterDdl(OctopusSqlParser.DdlContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#ddl}.
	 * @param ctx the parse tree
	 */
	void exitDdl(OctopusSqlParser.DdlContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#ddlStmts}.
	 * @param ctx the parse tree
	 */
	void enterDdlStmts(OctopusSqlParser.DdlStmtsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#ddlStmts}.
	 * @param ctx the parse tree
	 */
	void exitDdlStmts(OctopusSqlParser.DdlStmtsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#ddlStmt}.
	 * @param ctx the parse tree
	 */
	void enterDdlStmt(OctopusSqlParser.DdlStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#ddlStmt}.
	 * @param ctx the parse tree
	 */
	void exitDdlStmt(OctopusSqlParser.DdlStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#alterSystem}.
	 * @param ctx the parse tree
	 */
	void enterAlterSystem(OctopusSqlParser.AlterSystemContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#alterSystem}.
	 * @param ctx the parse tree
	 */
	void exitAlterSystem(OctopusSqlParser.AlterSystemContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#dataSourceClause}.
	 * @param ctx the parse tree
	 */
	void enterDataSourceClause(OctopusSqlParser.DataSourceClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#dataSourceClause}.
	 * @param ctx the parse tree
	 */
	void exitDataSourceClause(OctopusSqlParser.DataSourceClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#dataSourceName}.
	 * @param ctx the parse tree
	 */
	void enterDataSourceName(OctopusSqlParser.DataSourceNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#dataSourceName}.
	 * @param ctx the parse tree
	 */
	void exitDataSourceName(OctopusSqlParser.DataSourceNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#jdbcConnectionString}.
	 * @param ctx the parse tree
	 */
	void enterJdbcConnectionString(OctopusSqlParser.JdbcConnectionStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#jdbcConnectionString}.
	 * @param ctx the parse tree
	 */
	void exitJdbcConnectionString(OctopusSqlParser.JdbcConnectionStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#createUser}.
	 * @param ctx the parse tree
	 */
	void enterCreateUser(OctopusSqlParser.CreateUserContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#createUser}.
	 * @param ctx the parse tree
	 */
	void exitCreateUser(OctopusSqlParser.CreateUserContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#alterUser}.
	 * @param ctx the parse tree
	 */
	void enterAlterUser(OctopusSqlParser.AlterUserContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#alterUser}.
	 * @param ctx the parse tree
	 */
	void exitAlterUser(OctopusSqlParser.AlterUserContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#dropUser}.
	 * @param ctx the parse tree
	 */
	void enterDropUser(OctopusSqlParser.DropUserContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#dropUser}.
	 * @param ctx the parse tree
	 */
	void exitDropUser(OctopusSqlParser.DropUserContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#user}.
	 * @param ctx the parse tree
	 */
	void enterUser(OctopusSqlParser.UserContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#user}.
	 * @param ctx the parse tree
	 */
	void exitUser(OctopusSqlParser.UserContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#password}.
	 * @param ctx the parse tree
	 */
	void enterPassword(OctopusSqlParser.PasswordContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#password}.
	 * @param ctx the parse tree
	 */
	void exitPassword(OctopusSqlParser.PasswordContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#oldPassword}.
	 * @param ctx the parse tree
	 */
	void enterOldPassword(OctopusSqlParser.OldPasswordContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#oldPassword}.
	 * @param ctx the parse tree
	 */
	void exitOldPassword(OctopusSqlParser.OldPasswordContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#createRole}.
	 * @param ctx the parse tree
	 */
	void enterCreateRole(OctopusSqlParser.CreateRoleContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#createRole}.
	 * @param ctx the parse tree
	 */
	void exitCreateRole(OctopusSqlParser.CreateRoleContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#dropRole}.
	 * @param ctx the parse tree
	 */
	void enterDropRole(OctopusSqlParser.DropRoleContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#dropRole}.
	 * @param ctx the parse tree
	 */
	void exitDropRole(OctopusSqlParser.DropRoleContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#role}.
	 * @param ctx the parse tree
	 */
	void enterRole(OctopusSqlParser.RoleContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#role}.
	 * @param ctx the parse tree
	 */
	void exitRole(OctopusSqlParser.RoleContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#showDataSources}.
	 * @param ctx the parse tree
	 */
	void enterShowDataSources(OctopusSqlParser.ShowDataSourcesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#showDataSources}.
	 * @param ctx the parse tree
	 */
	void exitShowDataSources(OctopusSqlParser.ShowDataSourcesContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#showSchemas}.
	 * @param ctx the parse tree
	 */
	void enterShowSchemas(OctopusSqlParser.ShowSchemasContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#showSchemas}.
	 * @param ctx the parse tree
	 */
	void exitShowSchemas(OctopusSqlParser.ShowSchemasContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#showTables}.
	 * @param ctx the parse tree
	 */
	void enterShowTables(OctopusSqlParser.ShowTablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#showTables}.
	 * @param ctx the parse tree
	 */
	void exitShowTables(OctopusSqlParser.ShowTablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#showColumns}.
	 * @param ctx the parse tree
	 */
	void enterShowColumns(OctopusSqlParser.ShowColumnsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#showColumns}.
	 * @param ctx the parse tree
	 */
	void exitShowColumns(OctopusSqlParser.ShowColumnsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#showTablePrivileges}.
	 * @param ctx the parse tree
	 */
	void enterShowTablePrivileges(OctopusSqlParser.ShowTablePrivilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#showTablePrivileges}.
	 * @param ctx the parse tree
	 */
	void exitShowTablePrivileges(OctopusSqlParser.ShowTablePrivilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#showColumnPrivileges}.
	 * @param ctx the parse tree
	 */
	void enterShowColumnPrivileges(OctopusSqlParser.ShowColumnPrivilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#showColumnPrivileges}.
	 * @param ctx the parse tree
	 */
	void exitShowColumnPrivileges(OctopusSqlParser.ShowColumnPrivilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#showUsers}.
	 * @param ctx the parse tree
	 */
	void enterShowUsers(OctopusSqlParser.ShowUsersContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#showUsers}.
	 * @param ctx the parse tree
	 */
	void exitShowUsers(OctopusSqlParser.ShowUsersContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#commentOn}.
	 * @param ctx the parse tree
	 */
	void enterCommentOn(OctopusSqlParser.CommentOnContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#commentOn}.
	 * @param ctx the parse tree
	 */
	void exitCommentOn(OctopusSqlParser.CommentOnContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#commentOnTarget}.
	 * @param ctx the parse tree
	 */
	void enterCommentOnTarget(OctopusSqlParser.CommentOnTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#commentOnTarget}.
	 * @param ctx the parse tree
	 */
	void exitCommentOnTarget(OctopusSqlParser.CommentOnTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#dataSource}.
	 * @param ctx the parse tree
	 */
	void enterDataSource(OctopusSqlParser.DataSourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#dataSource}.
	 * @param ctx the parse tree
	 */
	void exitDataSource(OctopusSqlParser.DataSourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#schemaPattern}.
	 * @param ctx the parse tree
	 */
	void enterSchemaPattern(OctopusSqlParser.SchemaPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#schemaPattern}.
	 * @param ctx the parse tree
	 */
	void exitSchemaPattern(OctopusSqlParser.SchemaPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#tablePattern}.
	 * @param ctx the parse tree
	 */
	void enterTablePattern(OctopusSqlParser.TablePatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#tablePattern}.
	 * @param ctx the parse tree
	 */
	void exitTablePattern(OctopusSqlParser.TablePatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#columnPattern}.
	 * @param ctx the parse tree
	 */
	void enterColumnPattern(OctopusSqlParser.ColumnPatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#columnPattern}.
	 * @param ctx the parse tree
	 */
	void exitColumnPattern(OctopusSqlParser.ColumnPatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void enterSchemaName(OctopusSqlParser.SchemaNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#schemaName}.
	 * @param ctx the parse tree
	 */
	void exitSchemaName(OctopusSqlParser.SchemaNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(OctopusSqlParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(OctopusSqlParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(OctopusSqlParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(OctopusSqlParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(OctopusSqlParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(OctopusSqlParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link OctopusSqlParser#error}.
	 * @param ctx the parse tree
	 */
	void enterError(OctopusSqlParser.ErrorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OctopusSqlParser#error}.
	 * @param ctx the parse tree
	 */
	void exitError(OctopusSqlParser.ErrorContext ctx);
}