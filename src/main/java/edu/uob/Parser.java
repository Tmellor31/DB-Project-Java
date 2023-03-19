package edu.uob;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
//Read in id when getting from file, but when making table generate it
//Things to do - finish off parser grammar
//Make DB server return correct message [OK] or [ERROR]


public class Parser {
    int count = 0;
    ArrayList<String> query;
    DatabaseList databaseList;
    final String[] comparators = new String[]{"==", ">", "<", ">=", "<=", "!=", "LIKE"};

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
        } else if (query.get(count).equalsIgnoreCase(("INSERT")) && (query.get(count + 1).equalsIgnoreCase("INTO"))) {
            insert();
        } else if (query.get(count).equalsIgnoreCase(("DROP"))) {
            drop();

        } else if (query.get(count).equalsIgnoreCase(("CREATE"))) {
            create();
        }
          else if (query.get(count).equalsIgnoreCase(("JOIN"))){
              join();
        }
        return false;
    }

    private void use() throws Exception {
        moveToNextToken();
        databaseList.setActiveDB(query.get(count).toLowerCase());
    }

    private void insert() throws Exception {
        count += 2; //Move 2 because of insert AND into
        Table targetTable = databaseList.getActiveDB().getTable(getCurrentToken());
    }

    private void drop() throws Exception {
        moveToNextToken();
        if (query.get(count).equalsIgnoreCase("DATABASE")) {
            moveToNextToken();
            databaseList.dropDatabase(getCurrentToken());
        }
        if (query.get(count).equalsIgnoreCase("TABLE")) {
            moveToNextToken();
            databaseList.getActiveDB().dropTable(getCurrentToken());
        }
    }


    private void create() throws Exception {
        moveToNextToken();

        if (query.get(count).equalsIgnoreCase("DATABASE")) {
            moveToNextToken();
            databaseList.createDatabase(getCurrentToken());
        }
        if (query.get(count).equalsIgnoreCase("TABLE")) {
            moveToNextToken();

            if (databaseList.getActiveDB().getTable(getCurrentToken()) != null) {
                throw new Exception("TABLE " + getCurrentToken() + " already exists");
            } else {
                databaseList.getActiveDB().createTable(getCurrentToken(), new ArrayList<String>());
            }
        }
    }

    private void join() throws Exception{
        moveToNextToken();
        Table firstTable = databaseList.getActiveDB().getTable(getCurrentToken());
        if (firstTable == null){
            throw new Exception("Table " + getCurrentToken() + " not found");
        }
        moveToNextToken();
        if (getCurrentToken() != "AND"){
            throw new Exception("AND not found in JOIN statement - received " + getCurrentToken());
        }
        moveToNextToken();
        Table secondTable = databaseList.getActiveDB().getTable(getCurrentToken());
        if (secondTable == null){
            throw new Exception("Table " + getCurrentToken() + " not found");
        }
        moveToNextToken();
        if (getCurrentToken() != "ON"){
            throw new Exception("ON not found in JOIN statement - received " + getCurrentToken());
        }
        moveToNextToken();


    }

    private String getCurrentToken() {
        return query.get(count).toLowerCase();
    }

    private void moveToNextToken(){
        count++;
    }

    private boolean isAttributeName(String statement){
        return true;
    }

    private boolean isCondition(String statement) {
        // Split the statement into tokens
        String[] tokens = statement.split("\\s+");

        if (tokens.length >= 2 && tokens[0].equals("SELECT") && statement.contains("WHERE")) {
            return true;
        }

        if (tokens.length >= 2 && tokens[0].equals("UPDATE") && statement.contains("WHERE")) {
            return true;
        }

        if (tokens.length >= 2 && tokens[0].equals("DELETE") && statement.contains("WHERE")) {
            return true;
        }

        if (statement.startsWith("(") && statement.endsWith(")")) {
            return true;
        }
        if (isComparator(statement)){
            return true;
        }

        return false;
    }

    private boolean isComparator(String statement) {
        for (String comparator : comparators) {
            if (statement.contains(comparator)) {
                return true;
            }
        }
        return false;
    }
}












