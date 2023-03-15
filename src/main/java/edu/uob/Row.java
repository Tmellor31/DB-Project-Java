package edu.uob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class Row {
    private HashMap<String, String> values;

    public Row(ArrayList<String> columnNames) {
        values = new HashMap<>();
        for (String columnName : columnNames){
            values.put(columnName,"");
        }
    }


    public HashMap<String, String> getValues() {
        return this.values;
    }

    public void setValue(String columnName, String value) {
        if (!this.values.containsKey(columnName)) {
            throw new NoSuchElementException("Key not found in HashMap: " + columnName);
        }
        this.values.put(columnName, value);
    }
}

