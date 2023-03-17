package edu.uob;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Parser {
    int count = 0;
    ArrayList <String> query;
    public LinkedHashMap<String, Database> databases;


    public Parser(ArrayList<String> query, LinkedHashMap<String,Database> databases) {
        this.query = query;
        this.databases = databases;
    }


    public boolean readCommand() throws Exception {
        System.out.println("first command token is " + query.get(0));

        //Check that the last token is ;
        int lastTokenNum = query.size();
        String lastToken = query.get(lastTokenNum - 1);
        if (lastToken.equals(";")) {
            System.out.println("true");
            return true;
        }
        //query.tokens.get(count) should equal the 'current command' - at the start it is equal to zero so the first input
        if (query.get(count).equalsIgnoreCase(("ALTER"))) {
            //alter(query);
        } else if (query.get(count).equalsIgnoreCase(("USE"))) {
            //use(query);
        } else if (query.get(count).equalsIgnoreCase(("INSERT"))) {
            //insert(query);
        } else if (query.get(count).equalsIgnoreCase(("CREATE"))) {
            create();
        }
        return false;
    }

    private void use() {
        System.out.println(count);
    }

    private void create() throws Exception {
        count++;

        if (query.get(count).equalsIgnoreCase("DATABASE")) {
            count++;
            System.out.println(query.get(count));

            if (databases.containsKey(query.get(count).toLowerCase())) {
                throw new Exception("Database " + query.get(count) + " already exists");
            }
            else {
                String databaseName = query.get(count).toLowerCase();
                Database database = new Database(databaseName);
                databases.put(databaseName,database);
                System.out.println(databases);
            }
        }

    }

}









