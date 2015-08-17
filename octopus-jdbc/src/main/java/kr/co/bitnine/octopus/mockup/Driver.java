package kr.co.bitnine.octopus.mockup;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Driver implements java.sql.Driver {
    static
    {
        try {
            Driver registeredDriver = new Driver();
            DriverManager.registerDriver(registeredDriver);
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    @Override
    public Connection connect(String s, Properties properties) throws SQLException {
        return new OctopusMockupConnection();
    }

    @Override
    public boolean acceptsURL(String s) throws SQLException {
        return s.startsWith("jdbc:octopus-mockup:");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
