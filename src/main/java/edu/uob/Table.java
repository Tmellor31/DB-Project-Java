package edu.uob;

import java.util.ArrayList;

//They never give ids - my table should always generate them


public class Table {
    private String name;
    private ArrayList<Row> rows;
    private ArrayList<Column> cols;

    public Table(String name, ArrayList<String> colNames) {
        this.name = name;
        this.cols = new ArrayList<>();

        for (String colName : colNames) {
            this.cols.add(new Column(colName));
        }

        this.rows = new ArrayList<>();
    }

    public void insertNewRow(ArrayList<String> row) {
        String nextID = Integer.toString(getNextID());
        row.add(0, nextID);
        String[] rowArray = row.toArray(new String[row.size()]);
        insertRow(rowArray);
    }

    public void insertRow(String[] rowArray) {

        if (rowArray.length != this.cols.size()) {
            throw new IllegalArgumentException("Row count does not match the amount of cols in file.");
        }

        Row row = new Row(getColumnNames());


        ArrayList<String> columnNames = getColumnNames();

        for (int i = 0; i < columnNames.size(); i++) {
            row.setValue(columnNames.get(i), rowArray[i]);
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
        if (rows.isEmpty()) {
            return 1; //First positive integer
        }
        Row last = rows.get(rows.size() - 1);
        String lastidString = last.getValues().get("id");
        int lastID = Integer.parseInt(lastidString);
        return lastID + 1;
    }

    public boolean colInTable(String column) {
        return this.cols.contains(column);
    }

    public String stringifyTable() {
        String output = " ";
        for (Column col : this.getColumns()) {
            output = output.concat(col.getName()).concat("\t");
        }
        output = output.concat("\n");
        for (Row row : this.getRows()) {
            for (String value : row.getValues().values()) {
                output = output.concat(value).concat("\t");
            }
            output = output.concat("\n");
        }
        return output;
    }

    public void addColumn(String columnName) throws Exception {
        if (this.colInTable(columnName)) {
            throw new Exception("Column " + columnName + " already found in table");
        }
        Column column = new Column(columnName);
        cols.add(column);
    }
}




