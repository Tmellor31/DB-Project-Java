package edu.uob;

import java.util.LinkedHashMap;

public class DatabaseList {
    LinkedHashMap<String,Database> databases;
    private String activeDB = "";

    public DatabaseList(){
        this.databases = new LinkedHashMap<>();
    }

    public void setActiveDB(String database) throws Exception {
        if (databases.containsKey(database)){
            this.activeDB = database;
        }
        else {
            throw new Exception(database + " does not exist");
        }
    }

    public Database getActiveDB() {
        return databases.get(activeDB);
    }

    public Database getDatabase(String key){
        return databases.get(key);
    }

    public void createDatabase(String key) throws Exception {
        if (!databases.containsKey(key)){
            Database database = new Database(key);
            databases.put(key,database);
        }
        else {
            throw new Exception("Database '" + key + "' already exists");
        }
    }
}
