package kr.co.bitnine.octopus.queryengine;

import kr.co.bitnine.octopus.schema.MetaStore;
import kr.co.bitnine.octopus.schema.model.MDataSource;
import org.apache.calcite.avatica.Meta;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataSourceManager {

    MetaStore metastore;

    public DataSourceManager (MetaStore metastore) {
        this.metastore = metastore;
    }

    Connection getDataSourceConnection(String datasource) throws Exception {
        /* TODO: connection pooling */

        /* suppose that we can connect to the datasource via JDBC connection */
        MDataSource mdataSource = metastore.getDatasource(datasource);

        return getJDBCconnection(mdataSource.getDriver(), mdataSource.getConnectionString());
    }

    private Connection getJDBCconnection(String driver, String connectionString) throws Exception {
        Class.forName(driver);
        return DriverManager.getConnection(connectionString);
    }
}
