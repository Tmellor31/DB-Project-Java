package edu.uob;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
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
        } else if (query.get(count).equalsIgnoreCase(("INSERT"))) {
            insert();
        } else if (query.get(count).equalsIgnoreCase(("DROP"))) {
            drop();

        } else if (query.get(count).equalsIgnoreCase(("CREATE"))) {
            create();
        } else if (query.get(count).equalsIgnoreCase(("SELECT"))) {
            return select();
        } else if (query.get(count).equalsIgnoreCase(("JOIN"))) {
            join();
        } else if (query.get(count).equalsIgnoreCase(("UPDATE"))) {
            update();
        }
        return "Boom";
    }

    private void use() throws Exception {
        moveToNextToken();
        databaseList.setActiveDB(query.get(count).toLowerCase());
    }

    private void insert() throws Exception {
        moveToNextToken();
        if (!getCurrentToken().equalsIgnoreCase("INTO")) {
            throw new Exception("Expected INTO after INSERT, received " + getCurrentToken());
        }
        moveToNextToken();
        if (!isPlainText(getCurrentToken())) {
            throw new Exception("Tables must be plain text, received " + getCurrentToken());
        }
        if (databaseList.getActiveDB().getTable(getCurrentToken().toLowerCase()) == null) {
            throw new Exception("TABLE " + getCurrentToken() + " does not exist");
        }
        Table targetTable = databaseList.getActiveDB().getTable(getCurrentToken());
        moveToNextToken();
        if (!getCurrentToken().equalsIgnoreCase("VALUES")) {
            throw new Exception("Expected VALUES after Tablename in Insert, received " + getCurrentToken());
        }
        moveToNextToken();
        if (!getCurrentToken().equals("(")) {
            throw new Exception("Expected '(' in insert, received " + getCurrentToken());
        }
        Integer closeBracketPosition = findNextToken(count, ")");
        moveToNextToken();
        ArrayList<String> valueList = getValueList(count, closeBracketPosition - 1);
        System.out.println("VALUE LIST IS " + valueList);
        count = closeBracketPosition; //Move to close bracket
        if (!getCurrentToken().equals(")")) {
            throw new Exception("Expected ')' in insert, received " + getCurrentToken());
        }
        targetTable.insertNewRow(valueList);


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
        moveToNextToken();
        String tableName = query.get(count).toLowerCase();
        if (!isPlainText(getCurrentToken())) {
            throw new Exception("Given table name is not plain text, received " + tableName);
        }
        Table activeTable = databaseList.getActiveDB().getTable(tableName);
        if (activeTable == null) {
            throw new Exception("Table " + tableName + " does not exist");
        }
        moveToNextToken();
        String command = getCurrentToken().toLowerCase();
        if (!command.equalsIgnoreCase("ADD") && !command.equalsIgnoreCase("DROP")) {
            throw new Exception(("ADD or DROP expected after ALTER, received " + getCurrentToken()));
        }
        moveToNextToken();
        String columnName = getCurrentToken();
        if (!isAttributeName(columnName)) {
            throw new Exception(("Expected attribute name after space in ALTER, received " + getCurrentToken()));
        }
        if (command.equalsIgnoreCase("ADD")) {
            add(columnName, activeTable);
        }
        if (command.equalsIgnoreCase("DROP")) {
            //dropColumn();
        }
    }

    private void add(String colName, Table table) throws Exception {
        table.addColumn(colName);
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

    private String select() throws Exception {
        moveToNextToken();
        System.out.println(getCurrentToken());
        if (!isWildAttributeList(count)) {
            throw new Exception("Expected * or an attribute list, receieved " + getCurrentToken());
        }
        moveToNextToken();
        if (!getCurrentToken().equalsIgnoreCase("FROM")) {
            throw new Exception("Expected FROM after wildattriblist in SELECT, received " + getCurrentToken());
        }
        moveToNextToken();
        String tableName = getCurrentToken();
        if (!isPlainText(tableName)) {
            throw new Exception("Expected a plain-text table in SELECT, received " + getCurrentToken());
        }
        Table activeTable = databaseList.getActiveDB().getTable(tableName);
        if (activeTable == null) {
            throw new Exception("Table " + tableName + " was not found in active database");
        }
        return activeTable.stringifyTable();
    }

    private void update() throws Exception {
        moveToNextToken();
        if (!isPlainText(getCurrentToken())) {
            throw new Exception("Plain text table not received in update, received " + getCurrentToken());
        }
        moveToNextToken();
        if (!getCurrentToken().equalsIgnoreCase("SET")) {
            throw new Exception("SET not found after table in update, received " + getCurrentToken());
        }
        moveToNextToken();
        if (!isNameValueList(count)) {
            throw new Exception("Name value list not found after SET in update, received " + getCurrentToken());
        }
        moveToNextToken();
        if (!getCurrentToken().equalsIgnoreCase("WHERE")) {
            throw new Exception("WHERE not found after namevaluelist in update, received " + getCurrentToken());
        }
        if (!isCondition(getCurrentToken())) {
            throw new Exception("Condition not found after WHERE in update, received " + getCurrentToken());
        }
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

    private String getValue(Integer currentPosition) throws Exception {
        String currentToken = query.get(currentPosition);
        if (currentToken.equalsIgnoreCase("NULL")) {
            return currentToken;
        } else if (isBooleanLiteral(currentToken)) {
            return currentToken;
        } else if (isIntegerLiteral(currentPosition)) {
            return currentToken;
        } else if (isFloatLiteral(currentPosition)) {
            return currentToken;
        } else if (currentToken.startsWith("'") && currentToken.endsWith("'")) {
            String strippedToken = currentToken.substring(1, currentToken.length() - 1);
            return strippedToken;
        }
        throw new Exception("Failed to parse value " + currentToken);
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

    public boolean isStringLiteral(String string) {
        String symbolString = "";
        for (String symbol : this.symbols) {
            symbolString = symbolString.concat("\\" + symbol);
        }

        return string.matches("[A-Za-z0-9" + symbolString + "]*");
    }


    private boolean isSymbol(String statement) {
        for (String symbol : symbols) {
            if (statement.contains(symbol)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIntegerLiteral(Integer currentPosition) {
        if (isDigitSequence(query.get(currentPosition))) {
            return true;
        } else if (query.get(currentPosition).equals("-") || query.get(currentPosition).equals("+") && isDigitSequence(query.get(currentPosition + 1))) {
            return true;
        }
        return false;
    }

    private boolean isFloatLiteral(Integer currentPosition) {
        if (isDigitSequence(query.get(currentPosition))) {
            if (query.get(currentPosition + 1).equals(".")) {
                return isDigitSequence(query.get(currentPosition + 2));
            }
            return false;
        } else if (query.get(currentPosition).equals("-") || query.get(currentPosition).equals("+")) {
            if (query.get(currentPosition + 1).equals(".") && isDigitSequence(query.get(currentPosition + 2))) {
                return true;
            }
            return false;
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

    private ArrayList<String> getValueList(Integer startPosition, Integer endPosition) throws Exception {
        ArrayList <String> valueList = new ArrayList<>();
        for (int currentPosition = startPosition; currentPosition < endPosition; currentPosition+=2) {
            String value = getValue(currentPosition);
             if (!query.get(currentPosition + 1).equals(",")) {
                throw new Exception("Not a comma, received " + query.get(currentPosition+1));
            }
             valueList.add(value);
        }
        valueList.add(getValue(endPosition));
        return valueList;
    }

    private boolean isNameValueList(Integer currentPosition) throws Exception {
        if (!isNameValuePair(currentPosition)) {
            return false;
        } else if (query.get(currentPosition + 1).equals(",") && isNameValueList(currentPosition + 2)) {
            return true;
        }
        return false;
    }


    public boolean isNameValuePair(Integer currentPosition) throws Exception {
        if (!isAttributeName(query.get(currentPosition))) {
            return false;
        }
        if (!query.get(currentPosition + 1).equals("=")) {
            return false;
        }
        String value = getValue(currentPosition);
        return true;
    }

    private boolean isAttributeList(Integer currentPosition) {
        if (!isAttributeName(query.get(currentPosition))) {
            return false;
        } else if (query.get(currentPosition + 1).equals(",") && isAttributeList(currentPosition + 2)) {
            return true;
        }
        return false;
    }

    private boolean isWildAttributeList(Integer currentPosition) {
        if (query.get(currentPosition).equals("*")) {
            return true;
        }
        return isAttributeList(currentPosition);
    }

    private boolean isAttributeName(String attribute) {
        String[] splitToken = attribute.split("\\.");
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


    private boolean isCondition(String statement) { //TODO Remove?
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
        if (statement.equals("AND") || statement.equals("OR")) {
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

    private Integer findNextToken(Integer currentPosition, String token) throws Exception {
        for (; currentPosition < query.size(); currentPosition++) {
            String currentToken = query.get(currentPosition);
            System.out.println(currentToken);
            if (currentToken.equals(token)) {
                return currentPosition;
            }
        }
        System.out.println(Arrays.toString(query.toArray()));
        throw new Exception("Token " + token + " not found in given query");
    }

}













