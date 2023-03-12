package edu.uob;

import java.util.ArrayList;

public class Table {
    ArrayList <String> rows;
    ArrayList <String> cols;
    String name;

    public Table(String name, ArrayList<String> cols) {
        this.name = name;
        this.cols = cols;
        this.rows = new ArrayList<String>();
    }

    public void insertRow(String row){
        rows.add(row);
    }
}
