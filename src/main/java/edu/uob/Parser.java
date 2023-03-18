package edu.uob;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Parser {
    int count = 0;
    ArrayList<String> query;
    DatabaseList databaseList;


    public Parser(ArrayList<String> query, DatabaseList databaseList) {
        this.query = query;
        this.databaseList = databaseList;
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
            use();
        } else if (query.get(count).equalsIgnoreCase(("INSERT"))) {
            //insert(query);
        } else if (query.get(count).equalsIgnoreCase(("CREATE"))) {
            create();
        }
        return false;
    }

    private void use() throws Exception {
        count++;
        databaseList.setActiveDB(query.get(count).toLowerCase());
    }
    private void create() throws Exception {
        count++;

        if (query.get(count).equalsIgnoreCase("DATABASE")) {
            count++;
            System.out.println(query.get(count));
            databaseList.createDatabase(query.get(count).toLowerCase());
        }
        if (query.get(count).equalsIgnoreCase("TABLE")) {
            count++;

            if (databaseList.getActiveDB().tableMap.containsKey(query.get(count))) {
                throw new Exception("TABLE " + query.get(count) + " already exists");
            }
        } //else {
            //databaseList.getActiveDB().tableMap.put(query)
        }
    }











