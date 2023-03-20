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
    final String[] symbols = new String[]{"!", "#", "$", "%", "&", "(", ")", "*", "+", ",", "-", ".", "/", ":", ";", ">", "=", "<", "?", "@", "[", "\",", "|", "]", "^", "_", "`", "{", "}", "~"};
    final String space = " ";

    public Parser(ArrayList<String> query, DatabaseList databaseList) {
        this.query = query;
        this.databaseList = databaseList;
    }


    public String readCommand() throws Exception {
        System.out.println("first command token is " + query.get(0));

        //Check that the last token is ;
        int lastTokenNum = query.size();
        String lastToken = query.get(lastTokenNum - 1);
        if (lastToken.equals(";")) {
            System.out.println("true");
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
        } else if (query.get(count).equalsIgnoreCase(("JOIN"))) {
            join();
        }
        return "Boom";
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

    private void join() throws Exception {
        moveToNextToken();
        Table firstTable = databaseList.getActiveDB().getTable(getCurrentToken());
        if (firstTable == null) {
            throw new Exception("Table " + getCurrentToken() + " not found");
        }
        moveToNextToken();
        if (getCurrentToken() != "AND") {
            throw new Exception("AND not found in JOIN statement - received " + getCurrentToken());
        }
        moveToNextToken();
        Table secondTable = databaseList.getActiveDB().getTable(getCurrentToken());
        if (secondTable == null) {
            throw new Exception("Table " + getCurrentToken() + " not found");
        }
        moveToNextToken();
        if (getCurrentToken() != "ON") {
            throw new Exception("ON not found in JOIN statement - received " + getCurrentToken());
        }
        moveToNextToken();
        String firstAttribute = getCurrentToken();
        if (!isAttributeName(firstAttribute)) {
            throw new Exception("Attribute name not found after ON in join statement, received " + firstAttribute);
        }
        moveToNextToken();
        if (getCurrentToken() != "AND") {
            throw new Exception("AND not found in Join statement, received " + getCurrentToken());
        }
        moveToNextToken();
        String secondAttribute = getCurrentToken();
        if (!isAttributeName(secondAttribute)) {
            throw new Exception("Attribute name not found after AND in join statement, received " + secondAttribute);
        }
        String firstColName = attributeInTable(firstTable, firstAttribute);
        String secondColName = attributeInTable(secondTable, secondAttribute);

    }

    private String attributeInTable(Table table, String attribute) throws Exception {
        if (attribute.contains(".")) {
            String[] splitToken = attribute.split(".");
            if (splitToken.length != 2) {
                throw new Exception("Invalid number of attributes received. Received " + attribute);
            }
            String tableName = splitToken[0];
            String column = splitToken[1];
            if (table.getName() != tableName) {
                throw new Exception("Table name in join invalid, expected " + table.getName() + " received " + tableName);
            }
            if (!table.colInTable(column)) {
                throw new Exception("Column name " + column + " not found in table " + table.getName());
            }
            return column;
        } else {
            if (!table.colInTable(attribute)) {
                throw new Exception("Column " + attribute + " not found in table " + table.getName());
            }
            return attribute;
        }
    }

    private String getCurrentToken() {
        return query.get(count).toLowerCase();
    }

    private void moveToNextToken() {
        count++;
    }

    private boolean isValue(String statement) {
        return true;
    }

    private boolean isDigitSequence(Integer currentTokenPosition) {
        Integer nextPosition = currentTokenPosition + 1;
        if (isDigit(query.get(currentTokenPosition)) && isDigitSequence(nextPosition)) {
            return true;
        }
        return false;
    }

    private boolean isCharLiteral(String statement) {
        if (statement == space || isLetter(statement) || isSymbol(statement) || isDigit(statement)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isSymbol(String statement) {
        for (String symbol : symbols) {
            if (statement.contains(symbol)) {
                return true;
            }
        }
        return false;
    }


    private boolean isBooleanLiteral(String statement) {
        if (statement.equalsIgnoreCase("TRUE") || statement.equalsIgnoreCase("FALSE")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAttributeList(String statement) {
        if (isAttributeName(getCurrentToken())) {
            return true;
        } else if (isAttributeName(getCurrentToken()) && query.get(count + 1) == "," && isAttributeList(query.get(count + 2))) {
            return true;
        }
        return false;
    }

    private boolean isAttributeName(String statement) {
        if (isPlainText(statement)) {
            return true;
        } else if (databaseList.getActiveDB().getTable(statement) != null && query.get(count + 1) == "." && isPlainText(query.get(count + 2))) {
            return true;
        }
        return false;
    }


    private boolean isPlainText(String statement) {
        if (isLetter(statement) || isDigit(statement))/*Needs to work for words too*/ {
            return true;
        }
        return false;
    }

    private boolean isLetter(String statement) {
        if (statement.matches("[a-zA-Z]")) {
            return true;
        }
        return false;
    }


    private boolean isDigit(String statement) {
        if (statement.matches("[0-9]")) {
            // statement contains only a single digit from 0 to 9
            return true;
        }
        return false;
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
        if (isComparator(statement)) {
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













