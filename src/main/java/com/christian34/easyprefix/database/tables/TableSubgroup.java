package com.christian34.easyprefix.database.tables;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
@DatabaseTable(tableName = "subgroups")
public class TableSubgroup {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.STRING, width = 64, unique = true)
    private String name;

    @DatabaseField(dataType = DataType.STRING, width = 128)
    private String prefix;

    @DatabaseField(dataType = DataType.STRING, width = 128)
    private String suffix;

    @SuppressWarnings("unused")
    public TableSubgroup() {

    }

    public TableSubgroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

}
