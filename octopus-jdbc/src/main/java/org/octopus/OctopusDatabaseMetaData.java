package org.octopus;

import org.postgresql.jdbc4.Jdbc4Connection;
import org.postgresql.jdbc4.Jdbc4DatabaseMetaData;

import java.sql.*;

public class OctopusDatabaseMetaData extends Jdbc4DatabaseMetaData implements java.sql.DatabaseMetaData {
    public OctopusDatabaseMetaData(Jdbc4Connection conn) {
        super(conn);
    }

    public java.sql.ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String types[]) throws SQLException
    {
        return null;
    }
}
