package edu.uob;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Database {
    private LinkedHashMap<String,Table> tableMap;
    String dbName = "Placeholder";

    public Database(String dbName){
      this.dbName = dbName;
      tableMap = new LinkedHashMap<>();
    }

    public void createNewTable(String tableName, ArrayList<String> colNames) throws Exception {
        colNames.add(0,"id");
        createTable(tableName, colNames);
    }

    public void createTable(String tableName, ArrayList<String> colNames) throws Exception {
        if (tableMap.containsKey(tableName)) {
            throw new Exception("Table " + tableName + " already exists");
        }
        else {
            Table table = new Table(tableName, colNames);
            tableMap.put(tableName,table);
        }
    }

    public LinkedHashMap<String, Table> getTables() {
        return tableMap;
    }

    public Table getTable(String key){
        return tableMap.get(key);
    }
    public void dropTable(String tableName) throws Exception{
        if (tableMap.containsKey(tableName)){
            tableMap.remove(tableName);
        }
        else {
            throw new Exception("Table " + tableName + " does not exist");
        }
    }
}

