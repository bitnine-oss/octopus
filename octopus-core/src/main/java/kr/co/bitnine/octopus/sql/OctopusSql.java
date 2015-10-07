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

import kr.co.bitnine.octopus.meta.privilege.ObjectPrivilege;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static kr.co.bitnine.octopus.sql.OctopusSqlObjectPrivileges.OctopusSqlGrantObjPrivs;
import static kr.co.bitnine.octopus.sql.OctopusSqlObjectPrivileges.OctopusSqlRevokeObjPrivs;
import static kr.co.bitnine.octopus.sql.OctopusSqlSystemPrivileges.OctopusSqlGrantSysPrivs;
import static kr.co.bitnine.octopus.sql.OctopusSqlSystemPrivileges.OctopusSqlRevokeSysPrivs;

public final class OctopusSql {
    private OctopusSql() { }

    private static class Listener extends OctopusSqlBaseListener {
        private final List<OctopusSqlCommand> commands;

        private Set<SystemPrivilege> sysPrivs;
        private Set<ObjectPrivilege> objPrivs;
        private OctopusSqlObjectTarget commentTarget;

        Listener() {
            commands = new ArrayList<>();
        }

        @Override
        public void exitDdl(OctopusSqlParser.DdlContext ctx) {
            if (ctx.exception != null)
                throw ctx.exception;
        }

        @Override
        public void exitParameterSet(OctopusSqlParser.ParameterSetContext ctx) {
            String confParam = ctx.parameterName().getText();
            String confValue = ctx.parameterValue().getText();

            commands.add(new OctopusSqlSet(confParam, confValue));
        }

        @Override
        public void exitAddDataSourceClause(OctopusSqlParser.AddDataSourceClauseContext ctx) {
            String dataSourceName = ctx.dataSourceName().getText();
            String jdbcConnectionString = ctx.jdbcConnectionString().getText();
            String jdbcDriverName = ctx.jdbcDriverName().getText();
            commands.add(new OctopusSqlAddDataSource(dataSourceName, jdbcConnectionString, jdbcDriverName));
        }

        @Override
        public void exitUpdateDataSource(OctopusSqlParser.UpdateDataSourceContext ctx) {
            String dataSourceName = ctx.dataSourceName().getText();
            commands.add(new OctopusSqlUpdateDataSource(dataSourceName));
        }

        @Override
        public void exitUpdateSchemas(OctopusSqlParser.UpdateSchemasContext ctx) {
            // FIXME: dataSourceName and schemaPattern will never be set to null
            String dataSourceName = ctx.dataSourceName() == null ? null : ctx.dataSourceName().getText();
            String schemaPattern = ctx.schemaPattern() == null ? null : ctx.schemaPattern().getText();
            commands.add(new OctopusSqlUpdateDataSource(dataSourceName, schemaPattern));
        }

        @Override
        public void exitUpdateTables(OctopusSqlParser.UpdateTablesContext ctx) {
            // FIXME: dataSourceName, schemaPattern and tablePattern will never be set to null
            String dataSourceName = ctx.dataSourceName() == null ? null : ctx.dataSourceName().getText();
            String schemaName = ctx.schemaName() == null ? null : ctx.schemaName().getText();
            String tablePattern = ctx.tablePattern() == null ? null : ctx.tablePattern().getText();
            commands.add(new OctopusSqlUpdateDataSource(dataSourceName, schemaName, tablePattern));
        }

        @Override
        public void exitDropDataSourceClause(OctopusSqlParser.DropDataSourceClauseContext ctx) {
            String dataSourceName = ctx.dataSourceName().getText();
            commands.add(new OctopusSqlDropDataSource(dataSourceName));
        }

        @Override
        public void exitCreateUser(OctopusSqlParser.CreateUserContext ctx) {
            String name = ctx.user().getText();
            String password = ctx.password().getText();
            commands.add(new OctopusSqlCreateUser(name, password));
        }

        @Override
        public void exitAlterUser(OctopusSqlParser.AlterUserContext ctx) {
            String name = ctx.user().getText();
            String password = ctx.password().getText();
            OctopusSqlParser.OldPasswordContext oldPasswordCtx = ctx.oldPassword();
            String oldPassword = oldPasswordCtx == null ? null : oldPasswordCtx.getText();
            commands.add(new OctopusSqlAlterUser(name, password, oldPassword));
        }

        @Override
        public void exitDropUser(OctopusSqlParser.DropUserContext ctx) {
            String name = ctx.user().getText();
            commands.add(new OctopusSqlDropUser(name));
        }

        @Override
        public void exitCreateRole(OctopusSqlParser.CreateRoleContext ctx) {
            String name = ctx.role().getText();
            commands.add(new OctopusSqlCreateRole(name));
        }

        @Override
        public void exitDropRole(OctopusSqlParser.DropRoleContext ctx) {
            String name = ctx.role().getText();
            commands.add(new OctopusSqlDropRole(name));
        }

        @Override
        public void exitGrantSystemPrivileges(OctopusSqlParser.GrantSystemPrivilegesContext ctx) {
            assert this.sysPrivs != null;

            List<SystemPrivilege> sysPrivsList = new ArrayList<>(this.sysPrivs);

            Set<String> grantees = new HashSet<>();
            for (OctopusSqlParser.GranteeContext grantee : ctx.grantees().grantee())
                grantees.add(grantee.getText());

            commands.add(new OctopusSqlGrantSysPrivs(sysPrivsList, new ArrayList<>(grantees)));

            this.sysPrivs = null;
        }

        @Override
        public void exitRevokeSystemPrivileges(OctopusSqlParser.RevokeSystemPrivilegesContext ctx) {
            assert this.sysPrivs != null;

            List<SystemPrivilege> sysPrivsList = new ArrayList<>(this.sysPrivs);

            Set<String> revokees = new HashSet<>();
            for (OctopusSqlParser.GranteeContext revokee : ctx.grantees().grantee())
                revokees.add(revokee.getText());

            commands.add(new OctopusSqlRevokeSysPrivs(sysPrivsList, new ArrayList<>(revokees)));

            this.sysPrivs = null;
        }

        @Override
        public void enterSystemPrivileges(OctopusSqlParser.SystemPrivilegesContext ctx) {
            assert sysPrivs == null;
            sysPrivs = new HashSet<>();
        }

        @Override
        public void exitSysPrivAlterSystem(OctopusSqlParser.SysPrivAlterSystemContext ctx) {
            assert sysPrivs != null;
            sysPrivs.add(SystemPrivilege.ALTER_SYSTEM);
        }

        @Override
        public void exitSysPrivSelectAnyTable(OctopusSqlParser.SysPrivSelectAnyTableContext ctx) {
            assert sysPrivs != null;
            sysPrivs.add(SystemPrivilege.SELECT_ANY_TABLE);
        }

        @Override
        public void exitSysPrivCreateUser(OctopusSqlParser.SysPrivCreateUserContext ctx) {
            assert sysPrivs != null;
            sysPrivs.add(SystemPrivilege.CREATE_USER);
        }

        @Override
        public void exitSysPrivAlterUser(OctopusSqlParser.SysPrivAlterUserContext ctx) {
            assert sysPrivs != null;
            sysPrivs.add(SystemPrivilege.ALTER_USER);
        }

        @Override
        public void exitSysPrivDropUser(OctopusSqlParser.SysPrivDropUserContext ctx) {
            assert sysPrivs != null;
            sysPrivs.add(SystemPrivilege.DROP_USER);
        }

        @Override
        public void exitSysPrivCommentAny(OctopusSqlParser.SysPrivCommentAnyContext ctx) {
            assert sysPrivs != null;
            sysPrivs.add(SystemPrivilege.COMMENT_ANY);
        }

        @Override
        public void exitSysPrivGrantAnyObjPriv(OctopusSqlParser.SysPrivGrantAnyObjPrivContext ctx) {
            assert sysPrivs != null;
            sysPrivs.add(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE);
        }

        @Override
        public void exitSysPrivGrantAnyPriv(OctopusSqlParser.SysPrivGrantAnyPrivContext ctx) {
            assert sysPrivs != null;
            sysPrivs.add(SystemPrivilege.GRANT_ANY_PRIVILEGE);
        }

        @Override
        public void exitSysPrivAllPrivs(OctopusSqlParser.SysPrivAllPrivsContext ctx) {
            assert sysPrivs != null;
            sysPrivs.addAll(Arrays.asList(SystemPrivilege.values()));
        }

        @Override
        public void exitGrantObjectPrivileges(OctopusSqlParser.GrantObjectPrivilegesContext ctx) {
            assert this.objPrivs != null;

            List<ObjectPrivilege> objPrivsList = new ArrayList<>(this.objPrivs);

            String[] objName = {
                ctx.object().dataSourceName().getText(),
                ctx.object().schemaName().getText()
            };

            Set<String> grantees = new HashSet<>();
            for (OctopusSqlParser.GranteeContext grantee : ctx.grantees().grantee())
                grantees.add(grantee.getText());

            commands.add(new OctopusSqlGrantObjPrivs(objPrivsList, objName, new ArrayList<>(grantees)));

            this.objPrivs = null;
        }

        @Override
        public void exitRevokeObjectPrivileges(OctopusSqlParser.RevokeObjectPrivilegesContext ctx) {
            assert this.objPrivs != null;

            List<ObjectPrivilege> objPrivsList = new ArrayList<>(this.objPrivs);

            String[] objName = {
                ctx.object().dataSourceName().getText(),
                ctx.object().schemaName().getText()
            };

            Set<String> revokees = new HashSet<>();
            for (OctopusSqlParser.GranteeContext revokee : ctx.grantees().grantee())
                revokees.add(revokee.getText());

            commands.add(new OctopusSqlRevokeObjPrivs(objPrivsList, objName, new ArrayList<>(revokees)));

            this.objPrivs = null;
        }

        @Override
        public void enterObjectPrivileges(OctopusSqlParser.ObjectPrivilegesContext ctx) {
            assert objPrivs == null;
            objPrivs = new HashSet<>();
        }

        @Override
        public void exitObjPrivSelect(OctopusSqlParser.ObjPrivSelectContext ctx) {
            assert objPrivs != null;
            objPrivs.add(ObjectPrivilege.SELECT);
        }

        @Override
        public void exitObjPrivComment(OctopusSqlParser.ObjPrivCommentContext ctx) {
            assert objPrivs != null;
            objPrivs.add(ObjectPrivilege.COMMENT);
        }

        @Override
        public void exitObjPrivAllPrivs(OctopusSqlParser.ObjPrivAllPrivsContext ctx) {
            assert objPrivs != null;
            objPrivs.addAll(Arrays.asList(ObjectPrivilege.values()));
        }

        @Override
        public void exitShowDataSources(OctopusSqlParser.ShowDataSourcesContext ctx) {
            commands.add(new OctopusSqlShow.DataSources());
        }

        @Override
        public void exitShowSchemas(OctopusSqlParser.ShowSchemasContext ctx) {
            String dataSourceName = ctx.dataSourceName() == null ? null : ctx.dataSourceName().getText();
            String schemaPattern = ctx.schemaPattern() == null ? null : ctx.schemaPattern().getText();
            commands.add(new OctopusSqlShow.Schemas(dataSourceName, schemaPattern));
        }

        @Override
        public void exitShowTables(OctopusSqlParser.ShowTablesContext ctx) {
            String dataSourceName = ctx.dataSourceName() == null ? null : ctx.dataSourceName().getText();
            String schemaPattern = ctx.schemaPattern() == null ? null : ctx.schemaPattern().getText();
            String tablePattern = ctx.tablePattern() == null ? null : ctx.tablePattern().getText();
            commands.add(new OctopusSqlShow.Tables(dataSourceName, schemaPattern, tablePattern));
        }

        @Override
        public void exitShowColumns(OctopusSqlParser.ShowColumnsContext ctx) {
            String dataSourceName = ctx.dataSourceName() == null ? null : ctx.dataSourceName().getText();
            String schemaPattern = ctx.schemaPattern() == null ? null : ctx.schemaPattern().getText();
            String tablePattern = ctx.tablePattern() == null ? null : ctx.tablePattern().getText();
            String columnPattern = ctx.columnPattern() == null ? null : ctx.columnPattern().getText();
            commands.add(new OctopusSqlShow.Columns(dataSourceName, schemaPattern, tablePattern, columnPattern));
        }

        @Override
        public void exitShowAllUsers(OctopusSqlParser.ShowAllUsersContext ctx) {
            commands.add(new OctopusSqlShow.AllUsers());
        }

        @Override
        public void exitShowObjPrivsFor(OctopusSqlParser.ShowObjPrivsForContext ctx) {
            String userName = ctx.user().getText();
            commands.add(new OctopusSqlShow.ObjPrivsFor(userName));
        }

        @Override
        public void exitShowTablePrivileges(OctopusSqlParser.ShowTablePrivilegesContext ctx) {
        }

        @Override
        public void exitShowColumnPrivileges(OctopusSqlParser.ShowColumnPrivilegesContext ctx) {
        }

        @Override
        public void exitShowComments(OctopusSqlParser.ShowCommentsContext ctx) {
            String commentPattern = ctx.commentPattern() == null ? null : ctx.commentPattern().getText();
            String dataSourcePattern = ctx.dataSourcePattern() == null ? null : ctx.dataSourcePattern().getText();
            String schemaPattern = ctx.schemaPattern() == null ? null : ctx.schemaPattern().getText();
            String tablePattern = ctx.tablePattern() == null ? null : ctx.tablePattern().getText();
            String columnPattern = ctx.columnPattern() == null ? null : ctx.columnPattern().getText();
            commands.add(new OctopusSqlShow.Comments(commentPattern, dataSourcePattern, schemaPattern, tablePattern, columnPattern));
        }

        @Override
        public void exitCommentOn(OctopusSqlParser.CommentOnContext ctx) {
            assert commentTarget != null;

            String comment = ctx.comment().getText();
            commands.add(new OctopusSqlCommentOn(commentTarget, comment));

            commentTarget = null;
        }

        @Override
        public void exitCommentDataSource(OctopusSqlParser.CommentDataSourceContext ctx) {
            assert commentTarget == null;

            commentTarget = new OctopusSqlObjectTarget();
            commentTarget.setType(OctopusSqlObjectTarget.Type.DATASOURCE);
            commentTarget.setDataSource(ctx.dataSourceName().getText());
        }

        @Override
        public void exitCommentSchema(OctopusSqlParser.CommentSchemaContext ctx) {
            assert commentTarget == null;

            commentTarget = new OctopusSqlObjectTarget();
            commentTarget.setType(OctopusSqlObjectTarget.Type.SCHEMA);
            commentTarget.setDataSource(ctx.dataSourceName().getText());
            commentTarget.setSchema(ctx.schemaName().getText());
        }

        @Override
        public void exitCommentTable(OctopusSqlParser.CommentTableContext ctx) {
            assert commentTarget == null;

            commentTarget = new OctopusSqlObjectTarget();
            commentTarget.setType(OctopusSqlObjectTarget.Type.TABLE);
            commentTarget.setDataSource(ctx.dataSourceName().getText());
            commentTarget.setSchema(ctx.schemaName().getText());
            commentTarget.setTable(ctx.tableName().getText());
        }

        @Override
        public void exitCommentColumn(OctopusSqlParser.CommentColumnContext ctx) {
            assert commentTarget == null;

            commentTarget = new OctopusSqlObjectTarget();
            commentTarget.setType(OctopusSqlObjectTarget.Type.COLUMN);
            commentTarget.setDataSource(ctx.dataSourceName().getText());
            commentTarget.setSchema(ctx.schemaName().getText());
            commentTarget.setTable(ctx.tableName().getText());
            commentTarget.setColumn(ctx.columnName().getText());
        }

        @Override
        public void exitCommentUser(OctopusSqlParser.CommentUserContext ctx) {
            assert commentTarget == null;

            commentTarget = new OctopusSqlObjectTarget();
            commentTarget.setType(OctopusSqlObjectTarget.Type.USER);
            commentTarget.setUser(ctx.user().getText());
        }

        @Override
        public void exitSetDataCategoryOn(OctopusSqlParser.SetDataCategoryOnContext ctx) {
            OctopusSqlObjectTarget target = new OctopusSqlObjectTarget();
            target.setType(OctopusSqlObjectTarget.Type.COLUMN);
            target.setDataSource(ctx.dataSourceName().getText());
            target.setSchema(ctx.schemaName().getText());
            target.setTable(ctx.tableName().getText());
            target.setColumn(ctx.columnName().getText());

            String category = ctx.category().getText();

            commands.add(new OctopusSqlSetDataCategoryOn(target, category));
        }

        List<OctopusSqlCommand> getSqlCommands() {
            return commands;
        }
    }

    public static List<OctopusSqlCommand> parse(String query) {
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

    public static TupleSet run(OctopusSqlCommand command, OctopusSqlRunner runner) throws Exception {
        switch (command.getType()) {
        case SET:
            OctopusSqlSet set = (OctopusSqlSet) command;
            runner.set(set.getConfParam(), set.getConfValue());
            break;
        case ADD_DATASOURCE:
            OctopusSqlAddDataSource addDataSource = (OctopusSqlAddDataSource) command;
            runner.addDataSource(addDataSource.getDataSourceName(),
                    addDataSource.getJdbcConnectionString(), addDataSource.getJdbcDriverName());
            break;
        case UPDATE_DATASOURCE:
            OctopusSqlUpdateDataSource updateDataSource = (OctopusSqlUpdateDataSource) command;
            runner.updateDataSource(updateDataSource.getTarget());
            break;
        case DROP_DATASOURCE:
            OctopusSqlDropDataSource dropDataSource = (OctopusSqlDropDataSource) command;
            runner.dropDataSource(dropDataSource.getDataSourceName());
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
        case GRANT_SYS_PRIVS:
            OctopusSqlGrantSysPrivs grantSys = (OctopusSqlGrantSysPrivs) command;
            runner.grantSystemPrivileges(grantSys.getSysPrivs(), grantSys.getGrantees());
            break;
        case REVOKE_SYS_PRIVS:
            OctopusSqlRevokeSysPrivs revokeSys = (OctopusSqlRevokeSysPrivs) command;
            runner.revokeSystemPrivileges(revokeSys.getSysPrivs(), revokeSys.getGrantees());
            break;
        case GRANT_OBJ_PRIVS:
            OctopusSqlGrantObjPrivs grantObj = (OctopusSqlGrantObjPrivs) command;
            runner.grantObjectPrivileges(grantObj.getObjPrivs(), grantObj.getObjName(), grantObj.getGrantees());
            break;
        case REVOKE_OBJ_PRIVS:
            OctopusSqlRevokeObjPrivs revokeObj = (OctopusSqlRevokeObjPrivs) command;
            runner.revokeObjectPrivileges(revokeObj.getObjPrivs(), revokeObj.getObjName(), revokeObj.getGrantees());
            break;
        case SHOW_DATASOURCES:
            return runner.showDataSources();
        case SHOW_SCHEMAS:
            OctopusSqlShow.Schemas showSchemas = (OctopusSqlShow.Schemas) command;
            return runner.showSchemas(showSchemas.getDataSourceName(), showSchemas.getSchemaPattern());
        case SHOW_TABLES:
            OctopusSqlShow.Tables showTables = (OctopusSqlShow.Tables) command;
            return runner.showTables(showTables.getDataSourceName(), showTables.getSchemaPattern(), showTables.getTablePattern());
        case SHOW_COLUMNS:
            OctopusSqlShow.Columns showColumns = (OctopusSqlShow.Columns) command;
            return runner.showColumns(showColumns.getDataSourceName(), showColumns.getSchemaPattern(), showColumns.getTablePattern(), showColumns.getcolumnPattern());
        case SHOW_ALL_USERS:
            return runner.showAllUsers();
        case SHOW_OBJ_PRIVS_FOR:
            OctopusSqlShow.ObjPrivsFor showObjPrivsFor = (OctopusSqlShow.ObjPrivsFor) command;
            return runner.showObjPrivsFor(showObjPrivsFor.getUserName());
        case SHOW_COMMENTS:
            OctopusSqlShow.Comments showComments = (OctopusSqlShow.Comments) command;
            return runner.showComments(showComments.getCommentPattern(), showComments.getDataSourceName(),
                    showComments.getSchemaPattern(), showComments.getTablePattern(),
                    showComments.getcolumnPattern());
        case COMMENT_ON:
            OctopusSqlCommentOn commentOn = (OctopusSqlCommentOn) command;
            runner.commentOn(commentOn.getTarget(), commentOn.getComment());
            break;
        case SET_DATACATEGORY_ON:
            OctopusSqlSetDataCategoryOn setDataCategoryOn = (OctopusSqlSetDataCategoryOn) command;
            runner.setDataCategoryOn(setDataCategoryOn.getTarget(), setDataCategoryOn.getCategory());
            break;
        default:
            throw new RuntimeException("invalid Octopus SQL command");
        }
        return null;
    }
}
