// Generated from /home/kisung/octopus/octopus-core/src/main/antlr4/kr/co/bitnine/octopus/sql/OctopusSql.g4 by ANTLR 4.5.1

    import org.apache.commons.lang3.StringUtils;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link OctopusSqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface OctopusSqlVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#ddl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdl(OctopusSqlParser.DdlContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#ddlStmts}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdlStmts(OctopusSqlParser.DdlStmtsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#ddlStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdlStmt(OctopusSqlParser.DdlStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#alterSystem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterSystem(OctopusSqlParser.AlterSystemContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#dataSourceClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataSourceClause(OctopusSqlParser.DataSourceClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#dataSourceName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataSourceName(OctopusSqlParser.DataSourceNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#jdbcConnectionString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJdbcConnectionString(OctopusSqlParser.JdbcConnectionStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#createUser}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateUser(OctopusSqlParser.CreateUserContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#alterUser}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlterUser(OctopusSqlParser.AlterUserContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#dropUser}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropUser(OctopusSqlParser.DropUserContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#user}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUser(OctopusSqlParser.UserContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#password}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPassword(OctopusSqlParser.PasswordContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#oldPassword}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOldPassword(OctopusSqlParser.OldPasswordContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#createRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateRole(OctopusSqlParser.CreateRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#dropRole}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDropRole(OctopusSqlParser.DropRoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#role}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRole(OctopusSqlParser.RoleContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#showDataSources}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowDataSources(OctopusSqlParser.ShowDataSourcesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#showSchemas}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowSchemas(OctopusSqlParser.ShowSchemasContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#showTables}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowTables(OctopusSqlParser.ShowTablesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#showColumns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowColumns(OctopusSqlParser.ShowColumnsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#showTablePrivileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowTablePrivileges(OctopusSqlParser.ShowTablePrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#showColumnPrivileges}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowColumnPrivileges(OctopusSqlParser.ShowColumnPrivilegesContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#showUsers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowUsers(OctopusSqlParser.ShowUsersContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#commentOn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentOn(OctopusSqlParser.CommentOnContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#commentOnTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentOnTarget(OctopusSqlParser.CommentOnTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#dataSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataSource(OctopusSqlParser.DataSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#schemaPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaPattern(OctopusSqlParser.SchemaPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#tablePattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTablePattern(OctopusSqlParser.TablePatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#columnPattern}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnPattern(OctopusSqlParser.ColumnPatternContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#schemaName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaName(OctopusSqlParser.SchemaNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(OctopusSqlParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(OctopusSqlParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(OctopusSqlParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OctopusSqlParser#error}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitError(OctopusSqlParser.ErrorContext ctx);
}