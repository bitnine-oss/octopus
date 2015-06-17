package kr.co.bitnine.octopus.schema.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.Collection;

@PersistenceCapable
public class MTable {
    @PrimaryKey
    @Persistent(valueStrategy= IdGeneratorStrategy.INCREMENT)
    long ID;

    String name;
    int type;
    String description;
    String schema_name;
    MDataSource datasource;

    @Persistent(mappedBy = "table")
    Collection<MColumn> columns;

    public MTable(String name, int type, String description, String schema_name, MDataSource mds)
    {
        this.name = name;
        this.type = type;
        this.description = description;
        this.schema_name = schema_name;
        this.datasource = mds;
    }

    public int getColumnCnt() {
        return columns.size();
    }
}
