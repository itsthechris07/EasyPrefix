package com.christian34.easyprefix.database.tables;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "options")
public class TableOptions {

    @DatabaseField(generatedId = true, columnName = "id")
    private long id;

    @DatabaseField(dataType = DataType.STRING, width = 64, unique = true, canBeNull = false, columnName = "name")
    private String name;

    @DatabaseField(dataType = DataType.LONG_STRING, columnName = "value")
    private String value;

    @SuppressWarnings("unused")
    public TableOptions() {
    }

    public TableOptions(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
