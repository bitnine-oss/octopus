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

package kr.co.bitnine.octopus.sql;

import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;

public final class OctopusSql
{
    private OctopusSql() { }

    private static class Listener extends OctopusSqlBaseListener
    {
        private List<OctopusSqlCommand> commands;

        Listener()
        {
            commands = new ArrayList<>();
        }

        @Override
        public void exitDdl(OctopusSqlParser.DdlContext ctx)
        {
            if (ctx.exception != null)
                throw ctx.exception;
        }

        @Override
        public void exitDataSourceClause(OctopusSqlParser.DataSourceClauseContext ctx)
        {
            String dataSourceName = ctx.dataSourceName().getText();
            String jdbcConnectionString = ctx.jdbcConnectionString().getText();
            commands.add(new OctopusSqlAddDataSource(dataSourceName, jdbcConnectionString));
        }

        @Override
        public void exitCreateUser(OctopusSqlParser.CreateUserContext ctx)
        {
            String name = ctx.user().getText();
            String password = ctx.password().getText();
            commands.add(new OctopusSqlCreateUser(name, password));
        }

        @Override
        public void exitAlterUser(OctopusSqlParser.AlterUserContext ctx)
        {
            String name = ctx.user().getText();
            String password = ctx.password().getText();
            OctopusSqlParser.OldPasswordContext oldPasswordCtx = ctx.oldPassword();
            String oldPassword = oldPasswordCtx == null ? null : oldPasswordCtx.getText();
            commands.add(new OctopusSqlAlterUser(name, password, oldPassword));
        }

        @Override
        public void exitDropUser(OctopusSqlParser.DropUserContext ctx)
        {
            String name = ctx.user().getText();
            commands.add(new OctopusSqlDropUser(name));
        }

        @Override
        public void exitCreateRole(OctopusSqlParser.CreateRoleContext ctx)
        {
            String name = ctx.role().getText();
            commands.add(new OctopusSqlCreateRole(name));
        }

        @Override
        public void exitDropRole(OctopusSqlParser.DropRoleContext ctx)
        {
            String name = ctx.role().getText();
            commands.add(new OctopusSqlDropRole(name));
        }

        @Override
        public void exitShowDataSources(OctopusSqlParser.ShowDataSourcesContext ctx)
        {
        }

        @Override
        public void exitShowSchemas(OctopusSqlParser.ShowSchemasContext ctx)
        {

        }

        @Override
        public void exitShowTables(OctopusSqlParser.ShowTablesContext ctx)
        {
            String dataSource = ctx.dataSource() == null ? null : ctx.dataSource().getText();
            String schemaPattern = ctx.schemaPattern() == null ? null : ctx.schemaPattern().getText();
            String tablePattern = ctx.tablePattern() == null ? null : ctx.tablePattern().getText();
            commands.add(new OctopusSqlShowTables(dataSource, schemaPattern, tablePattern));
        }

        @Override
        public void exitShowColumns(OctopusSqlParser.ShowColumnsContext ctx)
        {
        }

        @Override
        public void exitShowUsers(OctopusSqlParser.ShowUsersContext ctx)
        {
        }

        @Override
        public void exitShowTablePrivileges(OctopusSqlParser.ShowTablePrivilegesContext ctx)
        {
        }

        @Override
        public void exitShowColumnPrivileges(OctopusSqlParser.ShowColumnPrivilegesContext ctx)
        {
        }

        @Override
        public void exitCommentOn(OctopusSqlParser.CommentOnContext ctx)
        {
		    OctopusSqlParser.CommentOnTargetContext targetCtx = ctx.commentOnTarget();
            OctopusSqlCommentOn.Target targetType = null;
            String target_name = null, comment = null;

            if (targetCtx.datasource() != null) {
                targetType = OctopusSqlCommentOn.Target.DATASOURCE;
                target_name = targetCtx.datasource().getText();
            }
            else if (targetCtx.schemaName() != null) {
                targetType = OctopusSqlCommentOn.Target.SCHEMA;
                target_name = targetCtx.schemaName().getText();
            }
            else if (targetCtx.tableName() != null) {
                targetType = OctopusSqlCommentOn.Target.TABLE;
                target_name = targetCtx.tableName().getText();
            }
            else if (targetCtx.tableName() != null) {
                targetType = OctopusSqlCommentOn.Target.COLUMN;
                target_name = targetCtx.columnName().getText();
            }

            comment = ctx.comment().getText();
            commands.add(new OctopusSqlCommentOn(targetType, target_name, comment));
        }

        List<OctopusSqlCommand> getSqlCommands()
        {
            return commands;
        }
    }

    public static List<OctopusSqlCommand> parse(String query)
    {
        ANTLRInputStream input = new ANTLRInputStream(query);
        OctopusSqlLexer lexer = new OctopusSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        OctopusSqlParser parser = new OctopusSqlParser(tokens);
        ParserRuleContext tree = parser.ddl();

        ParseTreeWalker walker = new ParseTreeWalker();
        Listener lsnr = new Listener();
        walker.walk(lsnr, tree);

        return lsnr.getSqlCommands();
    }

    public static TupleSet run(OctopusSqlCommand command, OctopusSqlRunner runner) throws Exception
    {
        switch (command.getType()) {
            case ADD_DATASOURCE:
                OctopusSqlAddDataSource addDataSource = (OctopusSqlAddDataSource) command;
                runner.addDataSource(addDataSource.getDataSourceName(), addDataSource.getJdbcConnectionString());
                break;
            case CREATE_USER:
                OctopusSqlCreateUser createUser = (OctopusSqlCreateUser) command;
                runner.createUser(createUser.getName(), createUser.getPassword());
                break;
            case ALTER_USER:
                OctopusSqlAlterUser alterUser = (OctopusSqlAlterUser) command;
                runner.alterUser(alterUser.getName(), alterUser.getPassword(), alterUser.getOldPassword());
                break;
            case DROP_USER:
                OctopusSqlDropUser dropUser = (OctopusSqlDropUser) command;
                runner.dropUser(dropUser.getName());
                break;
            case CREATE_ROLE:
                OctopusSqlCreateRole createRole = (OctopusSqlCreateRole) command;
                runner.createRole(createRole.getName());
                break;
            case DROP_ROLE:
                OctopusSqlDropRole dropRole = (OctopusSqlDropRole) command;
                runner.dropRole(dropRole.getName());
                break;
            case SHOW_TABLES:
                OctopusSqlShowTables showTables = (OctopusSqlShowTables) command;
<<<<<<< HEAD
                return runner.showTables(showTables.getDatasource(), showTables.getSchemapattern(), showTables.getTablepattern());
            case SHOW_USERS:
                OctopusSqlShowUsers showUsers = (OctopusSqlShowUsers) command;
                return runner.showUsers();
            case COMMENT_ON:
                OctopusSqlCommentOn commentOn = (OctopusSqlCommentOn) command;

=======
                return runner.showTables(showTables.getDataSource(), showTables.getSchemaPattern(), showTables.getTablePattern());
>>>>>>> 16402d0ed56e9f8ff5e5a2d89c9217c2e1d3d3db
            default:
                throw new RuntimeException("invalid Octopus SQL command");
        }
        return null;
    }
}
