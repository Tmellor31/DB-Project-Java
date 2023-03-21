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
    final String[] symbols = new String[]{"!", "#", "$", "%", "&", "(", ")", "*", "+", ",", "-", ".", "/", ":", ";", ">", "=", "<", "?", "@", "[", "\"", "|", "]", "^", "_", "`", "{", "}", "~"};
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
        if (!lastToken.equals(";")) {
            throw new Exception("Last token is not ;, received " + lastToken);
        }
        //query.tokens.get(count) should equal the 'current command' - at the start it is equal to zero so the first input
        if (query.get(count).equalsIgnoreCase(("ALTER"))) {
            alter();
        } else if (query.get(count).equalsIgnoreCase(("USE"))) {
            use();
        } else if (query.get(count).equalsIgnoreCase(("INSERT")) && (query.get(count + 1).equalsIgnoreCase("INTO"))) {
            insert();
        } else if (query.get(count).equalsIgnoreCase(("DROP"))) {
            drop();

        } else if (query.get(count).equalsIgnoreCase(("CREATE"))) {
            create();
        }
        else if (query.get(count).equalsIgnoreCase(("SELECT"))) {
            select();
        }else if (query.get(count).equalsIgnoreCase(("JOIN"))) {
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
            if (!isPlainText(getCurrentToken())) {
                throw new Exception("Database names must be plain text, received " + getCurrentToken());
            }
            databaseList.dropDatabase(getCurrentToken());
        }
        if (query.get(count).equalsIgnoreCase("TABLE")) {
            moveToNextToken();
            if (!isPlainText(getCurrentToken())) {
                throw new Exception("Table names must be plain text, received " + getCurrentToken());
            }
            databaseList.getActiveDB().dropTable(getCurrentToken());
        }
    }

    private void alter() throws Exception {
        moveToNextToken();
        if (!query.get(count).equalsIgnoreCase("TABLE")) {
            throw new Exception("Expected 'TABLE' after alter command, received " + getCurrentToken());
        }
        String tableName = query.get(count).toLowerCase();
        moveToNextToken();
        /*if (getCurrentToken().equalsIgnoreCase(ADD) {

        }*/
    }


    private void create() throws Exception {
        moveToNextToken();

        if (query.get(count).equalsIgnoreCase("DATABASE")) {
            moveToNextToken();
            if (!isPlainText(getCurrentToken())) {
                throw new Exception("Database names must be plain text, received " + getCurrentToken());
            }
            databaseList.createDatabase(getCurrentToken());

        }
        if (query.get(count).equalsIgnoreCase("TABLE")) {
            moveToNextToken();
            if (!isPlainText(getCurrentToken())) {
                throw new Exception("Table names must be plain text, received " + getCurrentToken());
            }
            if (databaseList.getActiveDB().getTable(getCurrentToken().toLowerCase()) != null) {
                throw new Exception("TABLE " + getCurrentToken() + " already exists");
            } else {
                databaseList.getActiveDB().createTable(getCurrentToken(), new ArrayList<String>());
            }

        }
    }

    private void select() throws Exception {
        moveToNextToken();
        if (!isWildAttributeList(count)){
            throw new Exception ("Expected * or an attribute list, receieved " + getCurrentToken());
        }
        moveToNextToken();
        if (getCurrentToken() != "FROM"){
            throw new Exception("Expected FROM after wildattriblist in SELECT, received " + getCurrentToken());
        }
        moveToNextToken();
        if (!isPlainText(getCurrentToken())){
            throw new Exception("Expected a plain-text table in SELECT, received " + getCurrentToken());
        }
        moveToNextToken();

    }

    private void join() throws Exception {
        moveToNextToken();
        Table firstTable = databaseList.getActiveDB().getTable(getCurrentToken().toLowerCase());
        if (firstTable == null) {
            throw new Exception("Table " + getCurrentToken() + " not found");
        }
        moveToNextToken();
        if (!getCurrentToken().equalsIgnoreCase("AND")) {
            throw new Exception("AND not found in JOIN statement - received " + getCurrentToken());
        }
        moveToNextToken();
        Table secondTable = databaseList.getActiveDB().getTable(getCurrentToken().toLowerCase());
        if (secondTable == null) {
            throw new Exception("Table " + getCurrentToken() + " not found");
        }
        moveToNextToken();
        if (!getCurrentToken().equalsIgnoreCase("ON")) {
            throw new Exception("ON not found in JOIN statement - received " + getCurrentToken());
        }
        moveToNextToken();
        String firstAttribute = getCurrentToken();
        if (!isAttributeName(firstAttribute)) {
            throw new Exception("Attribute name not found after ON in join statement, received " + firstAttribute);
        }
        moveToNextToken();
        if (!getCurrentToken().equalsIgnoreCase("AND")) {
            throw new Exception("AND not found in Join statement, received " + getCurrentToken());
        }
        moveToNextToken();
        String secondAttribute = getCurrentToken();
        if (!isAttributeName(secondAttribute)) {
            throw new Exception("Attribute name not found after AND in join statement, received " + secondAttribute);
        }
        //String firstColName = attributeInTable(firstTable, firstAttribute);
        //String secondColName = attributeInTable(secondTable, secondAttribute);

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

    private boolean isDigitSequence(String token) {
        return isNumber(token);
    }

    private boolean isCharLiteral(String statement) {
        if (statement == space || isLetter(statement) || isSymbol(statement) || isNumber(statement)) {
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

    private boolean isValueList(Integer currentPosition) {
        if (!isValue(query.get(currentPosition))) {
            return false;
        } else if (query.get(currentPosition + 1) == "," && isValueList(currentPosition + 2)) {
            return true;
        }
        return false;
    }

    public boolean isNameValuePair(Integer currentPosition) {
        if (!isAttributeName(query.get(currentPosition))) {
            return false;
        }
        if (query.get(currentPosition + 1) != "=") {
            return false;
        }
        if (!isValue(query.get(currentPosition + 2))) {
            return false;
        }
        return true;
    }

    private boolean isAttributeList(Integer currentPosition) {
        if (!isAttributeName(query.get(currentPosition))) {
            return false;
        } else if (query.get(currentPosition + 1) == "," && isAttributeList(currentPosition + 2)) {
            return true;
        }
        return false;
    }

    private boolean isWildAttributeList(Integer currentPosition) {
        if (query.get(currentPosition) == "*") {
            return true;
        }
        return isAttributeList(currentPosition);
    }

    private boolean isAttributeName(String attribute) {
        String[] splitToken = attribute.split(".");
        if (splitToken.length != 1 && splitToken.length != 2) {
            return false;
        }
        for (String token : splitToken) {
            if (!isPlainText(token)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPlainText(String token) {
        return token.matches("[a-zA-Z0-9]+");
    }

    private boolean isLetter(String token) {
        return token.matches("[a-zA-Z]+");
    }


    private boolean isNumber(String token) {
        return token.matches("[0-9]+");
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

    private boolean isBoolOperator(String statement) {
        if (statement == "AND" || statement == "OR") {
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













