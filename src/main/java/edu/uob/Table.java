package edu.uob;

import java.util.ArrayList;

//They never give ids - my table should always generate them


public class Table {
    private String name;
    private ArrayList<Row> rows;
    private ArrayList<Column> cols;
    private boolean hasIdColumn;

    public Table(String name, ArrayList<String> colNames, boolean hasIdColumn) {
        this.name = name;
        this.cols = new ArrayList<>();
        this.hasIdColumn = hasIdColumn;

        if (!hasIdColumn) {
            // Add "id" column to the beginning of the column list
            this.cols.add(new Column("id"));
        }

        for (String colName : colNames) {
            this.cols.add(new Column(colName));
        }

        this.rows = new ArrayList<>();
    }

    public void insertRow(String rowData) {
        String[] rowArray = rowData.split("\t");

        if (rowArray.length != this.cols.size() - (this.hasIdColumn ? 0 : 1)) {
            throw new IllegalArgumentException("Row count does not match the amount of cols in file.");
        }

        Row row = new Row(getColumnNames());

        if (!this.hasIdColumn) {
            // Add "id" value to the beginning of the row
            row.setValue("id", Integer.toString(getNextID()));
        }

        ArrayList<String> columnNames = getColumnNames();

        for (int i = this.hasIdColumn ? 0 : 1; i < columnNames.size(); i++) {
            row.setValue(columnNames.get(i), rowArray[i - (this.hasIdColumn ? 0 : 1)]);
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




