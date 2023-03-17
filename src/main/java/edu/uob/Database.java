package edu.uob;

import java.util.LinkedHashMap;

public class Database {
    LinkedHashMap<String,Table> tableMap;
    String dbName = "Placeholder";

    public Database(String dbName){
      this.dbName = dbName;
      tableMap = new LinkedHashMap<>();
    }
}

