package edu.uob;

import java.util.ArrayList;

public class Table {
    private String name;
    private ArrayList<Row> rows;
    private ArrayList<Column> cols;

    public Table(String name, ArrayList<String> colNames) {
        this.name = name;
        this.cols = new ArrayList<>();
        // Add "id" column to the beginning of the column list
        this.cols.add(new Column("id"));
        for (String colName : colNames) {
            this.cols.add(new Column(colName));
        }
        this.rows = new ArrayList<>();
    }

    public void insertRow(String rowData) {
        String[] rowArray = rowData.split("\t");
        //Always one more col because of id
        if (rowArray.length + 1 != this.cols.size()) {
            System.out.println(rowArray.length + 1);
            System.out.println(this.cols.size());
            throw new IllegalArgumentException("Row count does not match the amount of cols in file.");
        }
        // Add "id" value to the beginning of the row
        Row row = new Row(getColumnNames());
        row.setValue("id", Integer.toString(getNextID()));
        ArrayList<String> columnNames = getColumnNames();
        System.out.println(columnNames);
        //i starts at 1 to skip the id column
        for (int i = 1; i < columnNames.size(); i++) {
            System.out.println(i);
            row.setValue(columnNames.get(i), rowArray[i - 1]);
        }
        this.rows.add(row);
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Row> getRows() {
        return this.rows;
    }

    public ArrayList<Column> getColumns() {
        return this.cols;
    }

    public ArrayList<String> getColumnNames() {
        return this.cols.stream().map(column -> column.getName()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public int getNextID() {
        if (rows.isEmpty())
        {
            return 1; //First positive integer
        }
        Row last = rows.get(rows.size() - 1);
        String lastidString = last.getValues().get("id");
        int lastID = Integer.parseInt(lastidString);
        return lastID + 1;
    }
}
