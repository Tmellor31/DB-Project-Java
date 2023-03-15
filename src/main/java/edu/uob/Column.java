package edu.uob;

import java.util.ArrayList;

public class Column {
    private String name;
    private ArrayList<String> values;

    public Column(String name) {
        this.name = name;
        this.values = new ArrayList<>();
    }

    public void addValue(String value) {
        this.values.add(value);
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<String> getValues() {
        return this.values;
    }
}
