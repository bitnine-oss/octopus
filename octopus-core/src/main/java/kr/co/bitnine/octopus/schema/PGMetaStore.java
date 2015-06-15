package kr.co.bitnine.octopus.schema;

import kr.co.bitnine.octopus.pgproto.Exceptions;

import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by kisung on 15. 5. 20.
 */
public class PGMetaStore {

    HashMap<String, Connection> connPool;
    Connection _super_conn;

    public PGMetaStore() {
        try {
            // make a connection using postgreSQL superuser
            // this connection will be used for managing the database
            String url = "jdbc:postgresql://localhost/octopus?user=kisung&password=bitnine123";
            Class.forName("org.postgresql.Driver");
            _super_conn = DriverManager.getConnection(url);

        }
        catch (ClassNotFoundException e) {

        }
        catch (SQLException e) {

        }
    }

    public void loginUser(String user, String password) throws SQLException {
        String url = "jdbc:postgresql://localhost/octopus";
        Properties pros = new Properties();
        pros.setProperty("user", user);
        pros.setProperty("password", password);

        Connection conn = DriverManager.getConnection(url, pros);
    }

    public void createUser(String user, String password) {

    }

    public void insertTable() {

    }
}
