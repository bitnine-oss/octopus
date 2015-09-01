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
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.meta.model.MetaRole;
import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.meta.model.MetaUser;
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
    PersistenceManager pm;

    public JDOMetaContext(PersistenceManager persistenceManager)
    {
        pm = persistenceManager;
    }

    private MUser getUserByName(String name, boolean nothrow) throws MetaException
    {
        try {
            Query query = pm.newQuery(MUser.class);
            query.setFilter("name == '" + name + "'");
            query.setUnique(true);

            MUser user = (MUser) query.execute();
            if (user == null && !nothrow)
                throw new MetaException("user '" + name + "' does not exist");
            return user;
        } catch (RuntimeException e) {
            throw new MetaException(e);
        }
    }

    @Override
    public boolean userExists(String name) throws MetaException
    {
        return getUserByName(name, true) != null;
    }

    @Override
    public MetaUser createUser(String name, String password) throws MetaException
    {
        try {
            MUser user = new MUser(name, password);
            pm.makePersistent(user);
            return user;
        } catch (RuntimeException e) {
            throw new MetaException("failed to create user '" + name + "'");
        }
    }

    @Override
    public String getUserPasswordByName(String name) throws MetaException
    {
        return getUserByName(name, false).getPassword();
    }

    @Override
    public void alterUser(String name, String newPassword) throws MetaException
    {
        MUser user = getUserByName(name, false);
        user.setPassword(newPassword);
        pm.makePersistent(user);
    }

    @Override
    public void dropUser(String name) throws MetaException
    {
        pm.deletePersistent(getUserByName(name, false));
    }

    @Override
    public Collection<MetaUser> getUsers() throws MetaException
    {
        try {
            Query query = pm.newQuery(MUser.class);
            List<MetaUser> users = (List) query.execute();
            return users;
        } catch (RuntimeException e) {
            throw new MetaException(e);
        }
    }

    @Override
    public MetaDataSource addJdbcDataSource(String driverName, String connectionString, String name) throws MetaException
    {
        // TODO: check if it already exists

        try {
            Class.forName(driverName);
        } catch (ClassNotFoundException e) {
            throw new MetaException(e);
        }

        Transaction tx = pm.currentTransaction();
        try (Connection conn = DriverManager.getConnection(connectionString)) {
            DataContext dc = DataContextFactory.createJdbcDataContext(conn);

            tx.begin();

            MDataSource dataSource = new MDataSource(name, 0, driverName, connectionString, "");
            pm.makePersistent(dataSource);

            for (Schema rawSchema : dc.getSchemas()) {
                String schemaName = rawSchema.getName();
                if (schemaName == null)
                    schemaName = "__DEFAULT";

                MSchema schema = new MSchema(schemaName, dataSource);
                pm.makePersistent(schema);

                for (Table rawTable : rawSchema.getTables()) {
                    String tableName = rawTable.getName();

                    MTable table = new MTable(tableName, 0, "", schema);
                    pm.makePersistent(table);

                    for (Column rawColumn : rawTable.getColumns()) {
                        String columnName = rawColumn.getName();
                        int jdbcType = rawColumn.getType().getJdbcType();

                        MColumn column = new MColumn(columnName, jdbcType, "", -1, table);
                        pm.makePersistent(column);
                    }
                }
            }

            tx.commit();

            return dataSource;
        } catch (Exception e) {
            throw new MetaException(e);
        } finally {
            if (tx.isActive())
                tx.rollback();
        }
    }

    @Override
    public Collection<MetaDataSource> getDataSources() throws MetaException
    {
        try {
            Query query = pm.newQuery(MDataSource.class);
            List<MetaDataSource> dataSources = (List) query.execute();
            return dataSources;
        } catch (RuntimeException e) {
            throw new MetaException(e);
        }
    }

    @Override
    public MetaDataSource getDataSourceByName(String name) throws MetaException
    {
        try {
            Query query = pm.newQuery(MDataSource.class);
            query.setFilter("name == '" + name + "'");
            query.setUnique(true);

            MDataSource dataSource = (MDataSource) query.execute();
            if (dataSource == null)
                throw new MetaException("data source '" + name + "' does not exist");
            return dataSource;
        } catch (RuntimeException e) {
            throw new MetaException(e);
        }
    }

    @Override
    public MetaTable getTableByName(String name) throws MetaException
    {
        try {
            Query query = pm.newQuery(MTable.class);
            query.setFilter("name == '" + name + "'");
            query.setUnique(true);

            MTable table = (MTable) query.execute();
            if (table == null)
                throw new MetaException("table '" + name + "' does not exist");
            return table;
        } catch (RuntimeException e) {
            throw new MetaException(e);
        }
    }

    @Override
    public MetaRole createRole(String name) throws MetaException
    {
        try {
            MRole role = new MRole(name);
            pm.makePersistent(role);
            return role;
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

            MRole role = (MRole) query.execute();
            if (role == null)
                throw new MetaException("role '" + name + "' does not exist");

            pm.deletePersistent(role);
        } catch (RuntimeException e) {
            throw new MetaException(e);
        }
    }

    @Override
    public void close()
    {
        pm.close();
    }
}
