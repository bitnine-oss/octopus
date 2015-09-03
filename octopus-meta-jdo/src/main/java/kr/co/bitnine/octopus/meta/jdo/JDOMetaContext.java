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

package kr.co.bitnine.octopus.meta.jdo;

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.meta.jdo.model.*;
import kr.co.bitnine.octopus.meta.model.*;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.List;

public class JDOMetaContext implements MetaContext
{
    private static final Log LOG = LogFactory.getLog(JDOMetaContext.class);

    private final PersistenceManager pm;

    public JDOMetaContext(PersistenceManager persistenceManager)
    {
        pm = persistenceManager;
    }

    private MUser getMUser(String name, boolean nothrow) throws MetaException
    {
        try {
            Query query = pm.newQuery(MUser.class);
            query.setFilter("name == '" + name + "'");
            query.setUnique(true);

            MUser mUser = (MUser) query.execute();
            if (mUser == null && !nothrow)
                throw new MetaException("user '" + name + "' does not exist");
            return mUser;
        } catch (RuntimeException e) {
            throw new MetaException("failed to get user '" + name + "'", e);
        }
    }

    @Override
    public boolean userExists(String name) throws MetaException
    {
        return getMUser(name, true) != null;
    }

    @Override
    public MetaUser getUser(String name) throws MetaException
    {
        return getMUser(name, false);
    }

    @Override
    public MetaUser createUser(String name, String password) throws MetaException
    {
        try {
            MUser mUser = new MUser(name, password);
            pm.makePersistent(mUser);
            return mUser;
        } catch (RuntimeException e) {
            throw new MetaException("failed to create user '" + name + "'", e);
        }
    }

    @Override
    public void alterUser(String name, String newPassword) throws MetaException
    {
        MUser mUser = (MUser) getUser(name);
        mUser.setPassword(newPassword);
        try {
            pm.makePersistent(mUser);
        } catch (RuntimeException e) {
            throw new MetaException("failed to alter user '" + name + "'", e);
        }
    }

    @Override
    public void dropUser(String name) throws MetaException
    {
        try {
            pm.deletePersistent(getUser(name));
        } catch (RuntimeException e) {
            throw new MetaException("failed to drop user '" + name + "'", e);
        }
    }

    @Override
    public void commentOnUser(String comment, String name) throws MetaException
    {
        MUser mUser = (MUser) getUser(name);
        mUser.setComment(comment);
        try {
            pm.makePersistent(mUser);
        } catch (RuntimeException e) {
            throw new MetaException("failed to comment on user '" + name + "'", e);
        }
    }

    @Override
    public Collection<MetaUser> getUsers() throws MetaException
    {
        try {
            Query query = pm.newQuery(MUser.class);
            List<MetaUser> users = (List<MetaUser>) query.execute();
            return users;
        } catch (RuntimeException e) {
            throw new MetaException("failed to get user list", e);
        }
    }

    @Override
    public MetaDataSource addJdbcDataSource(String driverName, String connectionString, String name) throws MetaException
    {
        // TODO: check if it already exists

        LOG.debug("addJdbcDataSource. driverName=" + driverName + ", connectionString=" + connectionString + ", name=" + name);
        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            throw new MetaException(e);
        }


        Transaction tx = pm.currentTransaction();
        try (Connection conn = DriverManager.getConnection(connectionString)) {
            DataContext dc = DataContextFactory.createJdbcDataContext(conn);

            tx.begin();

            MDataSource dataSource = new MDataSource(name, 0, driverName, connectionString);
            pm.makePersistent(dataSource);

            for (Schema rawSchema : dc.getSchemas()) {
                String schemaName = rawSchema.getName();
                if (schemaName == null)
                    schemaName = "__DEFAULT";

                LOG.debug("add schema. schemaName=" + schemaName);
                MSchema schema = new MSchema(schemaName, dataSource);
                pm.makePersistent(schema);

                for (Table rawTable : rawSchema.getTables()) {
                    String tableName = rawTable.getName();

                    LOG.debug("add table. tableName=" + tableName);
                    MTable table = new MTable(tableName, "TABLE", schema); // FIXME: table type
                    pm.makePersistent(table);

                    for (Column rawColumn : rawTable.getColumns()) {
                        String columnName = rawColumn.getName();
                        int jdbcType = rawColumn.getType().getJdbcType();

                        LOG.debug("add column. columnName=" + columnName + ", jdbcType=" + jdbcType);
                        MColumn column = new MColumn(columnName, jdbcType, table);
                        pm.makePersistent(column);
                    }
                }
            }

            tx.commit();

            LOG.debug("complete addJdbcDataSource");
            return dataSource;
        } catch (Exception e) {
            throw new MetaException("failed to add data source" , e);
        } finally {
            if (tx.isActive())
                tx.rollback();
        }
    }

    @Override
    public MetaDataSource getDataSource(String name) throws MetaException
    {
        try {
            Query query = pm.newQuery(MDataSource.class);
            query.setFilter("name == '" + name + "'");
            query.setUnique(true);

            MDataSource mDataSource = (MDataSource) query.execute();
            if (mDataSource == null)
                throw new MetaException("data source '" + name + "' does not exist");
            return mDataSource;
        } catch (RuntimeException e) {
            throw new MetaException("failed to get data source '" + name + "'", e);
        }
    }

    @Override
    public void commentOnDataSource(String comment, String name) throws MetaException
    {
        MDataSource mDataSource = (MDataSource) getDataSource(name);
        mDataSource.setComment(comment);
        try {
            pm.makePersistent(mDataSource);
        } catch (RuntimeException e) {
            throw new MetaException("failed to comment on data source '" + name + "'", e);
        }
    }

    @Override
    public Collection<MetaDataSource> getDataSources() throws MetaException
    {
        try {
            Query query = pm.newQuery(MDataSource.class);
            List<MetaDataSource> dataSources = (List<MetaDataSource>) query.execute();
            return dataSources;
        } catch (RuntimeException e) {
            throw new MetaException(e);
        }
    }

    @Override
    public MetaSchema getSchemaByQualifiedName(String dataSourceName, String schemaName) throws MetaException
    {
        try {
            Query query = pm.newQuery(MSchema.class);
            query.setFilter("name == '" + schemaName + "'");

            List<MSchema> mSchemas = (List<MSchema>) query.execute();
            for (MSchema mSchema : mSchemas) {
                if (mSchema.getDataSource().getName().equals(dataSourceName))
                    return mSchema;
            }

            throw new MetaException("schema '" + dataSourceName + "." + schemaName + "' does not exist");
        } catch (RuntimeException e) {
            throw new MetaException("failed to get schema '" + dataSourceName + "." + schemaName + "'", e);
        }
    }

    @Override
    public void commentOnSchema(String comment, String dataSourceName, String schemaName) throws MetaException
    {
        MSchema mSchema = (MSchema) getSchemaByQualifiedName(dataSourceName, schemaName);
        mSchema.setComment(comment);
        try {
            pm.makePersistent(mSchema);
        } catch (RuntimeException e) {
            throw new MetaException("failed to comment on schema '" + dataSourceName + "." + schemaName + "'", e);
        }
    }

    @Override
    public MetaTable getTableByQualifiedName(String dataSourceName, String schemaName, String tableName) throws MetaException
    {
        try {
            Query query = pm.newQuery(MTable.class);
            query.setFilter("name == '" + tableName + "'");

            List<MTable> mTables = (List<MTable>) query.execute();
            for (MTable mTable : mTables) {
                MetaSchema schema = mTable.getSchema();
                MetaDataSource dataSource = schema.getDataSource();

                if (schema.getName().equals(schemaName) &&
                        dataSource.getName().equals(dataSourceName))
                    return mTable;
            }

            throw new MetaException("table '" + dataSourceName + "." + schemaName + "." + tableName +"' does not exist");
        } catch (RuntimeException e) {
            throw new MetaException("failed to get table '" + dataSourceName + "." + schemaName + "." + tableName + "'", e);
        }
    }

    @Override
    public void commentOnTable(String comment, String dataSourceName, String schemaName, String tableName) throws MetaException
    {
        MTable mTable = (MTable) getTableByQualifiedName(dataSourceName, schemaName, tableName);
        mTable.setComment(comment);
        try {
            pm.makePersistent(mTable);
        } catch (RuntimeException e) {
            throw new MetaException("failed to comment on table '" + dataSourceName + "." + schemaName + "." + tableName + "'", e);
        }
    }

    @Override
    public MetaColumn getColumnByQualifiedName(String dataSourceName, String schemaName, String tableName, String columnName) throws MetaException
    {
        try {
            Query query = pm.newQuery(MColumn.class);
            query.setFilter("name == '" + columnName + "'");

            List<MColumn> mColumns = (List<MColumn>) query.execute();
            for (MColumn mColumn : mColumns) {
                MetaTable table = mColumn.getTable();
                MetaSchema schema = table.getSchema();
                MetaDataSource dataSource = schema.getDataSource();

                if (table.getName().equals(tableName) &&
                        schema.getName().equals(schemaName) &&
                        dataSource.getName().equals(dataSourceName))
                    return mColumn;
            }

            throw new MetaException("column '" + dataSourceName + "." + schemaName + "." + tableName + "." + columnName + "' does not exist");
        } catch (RuntimeException e) {
            throw new MetaException("failed to get column '" + dataSourceName + "." + schemaName + "." + tableName + "." + columnName + "'", e);
        }
    }

    @Override
    public void commentOnColumn(String comment, String dataSourceName, String schemaName, String tableName, String columnName) throws MetaException
    {
        MColumn mColumn = (MColumn) getColumnByQualifiedName(dataSourceName, schemaName, tableName, columnName);
        mColumn.setComment(comment);
        try {
            pm.makePersistent(mColumn);
        } catch (RuntimeException e) {
            throw new MetaException("failed to comment on column '" + dataSourceName + "." + schemaName + "." + tableName + "." + columnName + "'", e);
        }
    }

    @Override
    public void setDataCategoryOn(String category, String dataSourceName, String schemaName, String tableName, String columnName) throws MetaException
    {
        MColumn mColumn = (MColumn) getColumnByQualifiedName(dataSourceName, schemaName, tableName, columnName);
        mColumn.setDataCategory(category);
        try {
            pm.makePersistent(mColumn);
        } catch (RuntimeException e) {
            throw new MetaException("failed to set data category on column '" + dataSourceName + "." + schemaName + "." + tableName + "." + columnName + "'", e);
        }
    }

    @Override
    public MetaRole createRole(String name) throws MetaException
    {
        try {
            MRole mRole = new MRole(name);
            pm.makePersistent(mRole);
            return mRole;
        } catch (RuntimeException e) {
            throw new MetaException("failed to create role '" + name + "'");
        }
    }

    @Override
    public void dropRoleByName(String name) throws MetaException
    {
        try {
            Query query = pm.newQuery(MRole.class);
            query.setFilter("name == '" + name + "'");
            query.setUnique(true);

            MRole mRole = (MRole) query.execute();
            if (mRole == null)
                throw new MetaException("role '" + name + "' does not exist");

            pm.deletePersistent(mRole);
        } catch (RuntimeException e) {
            throw new MetaException(e);
        }
    }

    @Override
    public void addSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> userNames) throws MetaException
    {
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();

            for (String userName : userNames) {
                MUser mUser = (MUser) getUser(userName);
                for (SystemPrivilege sysPriv : sysPrivs)
                    mUser.addSystemPrivilege(sysPriv);

                pm.makePersistent(mUser);
            }

            tx.commit();
        } catch (Exception e) {
            throw new MetaException("failed to add system privileges to users", e);
        } finally {
            if (tx.isActive())
                tx.rollback();
        }
    }

    @Override
    public void removeSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> userNames) throws MetaException
    {
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();

            for (String userName : userNames) {
                MUser mUser = (MUser) getUser(userName);
                for (SystemPrivilege sysPriv : sysPrivs)
                    mUser.removeSystemPrivilege(sysPriv);

                pm.makePersistent(mUser);
            }

            tx.commit();
        } catch (Exception e) {
            throw new MetaException("failed to remove system privileges from users", e);
        } finally {
            if (tx.isActive())
                tx.rollback();
        }
    }

    @Override
    public void close()
    {
        pm.close();
    }
}
