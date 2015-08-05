/*-------------------------------------------------------------------------
*
* Copyright (c) 2004-2014, PostgreSQL Global Development Group
*
*
*-------------------------------------------------------------------------
*/
package org.octopus;

import org.postgresql.jdbc4.Jdbc4Connection;
import org.postgresql.util.HostSpec;

import java.sql.SQLException;
import java.util.Properties;

public class OctopusConnection extends Jdbc4Connection {

    public OctopusConnection(HostSpec[] hostSpecs, String user, String database, Properties info, String url) throws SQLException {
        super(hostSpecs, user, database, info, url);
    }

    /* we override getMetaData to return OctopusDatabaseMetaData
     * this is main reason to make this class */
    public java.sql.DatabaseMetaData getMetaData() throws SQLException {
        checkClosed();
        if (metadata == null)
            metadata = new OctopusDatabaseMetaData(this);
        return metadata;
    }
}
