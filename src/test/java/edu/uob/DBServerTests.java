package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.Duration;


public class DBServerTests {
    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    private String generateRandomName()
    {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }
    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
                    return server.handleCommand(command);
                },
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    /*@Test //Had to remove as method is private, but tests ran successfully
    public void testStringLiteral(){
        String string = "#?fred1*";
        String notLiteralString = "≠";
        assertTrue(Parser.isStringLiteral(string));
        assertFalse(Parser.isStringLiteral(notLiteralString));
    }*/

    @Test
    public void testPlainTextDBandTable() {
        sendCommandToServer("CREATE DATABASE " + "fred" + ";");
        sendCommandToServer("USE " + "fred" + ";");
        sendCommandToServer("CREATE TABLE " + "fred" + ";");
        sendCommandToServer("CREATE TABLE " + ".//../../" + ";");
        String response = sendCommandToServer("CREATE DATABASE " + "...,.,.///" + ";");
        String response2 = sendCommandToServer("CREATE TABLE " + ".//../../" + ";");
        String response3 = sendCommandToServer("DROP DATABASE " + "...,.,.///" + ";");
        String response4 = sendCommandToServer("DROP TABLE " + "...,.,.///" + ";");
        String response5 = sendCommandToServer("DROP TABLE " + "fred" + ";");
        String response6 = sendCommandToServer("DROP DATABASE " + "fred" + ";");
        assertTrue(response.contains("[ERROR]"), "An ERROR tag was not returned after trying to create a non-plain text db");
        assertTrue(response2.contains("[ERROR]"), "An ERROR tag was not returned after trying to create a non-plain text table");
        assertTrue(response3.contains("[ERROR]"), "An ERROR tag was not returned after trying to drop a non-plain text db");
        assertTrue(response4.contains("[ERROR]"), "An ERROR tag was not returned after trying to drop a non-plain text table");
        assertTrue(response5.contains("[OK]"), "An OK tag was not returned after trying to drop a plain text table");
        assertTrue(response6.contains("[OK]"), "An OK tag was not returned after trying to drop a plain text db");
    }

    @Test
    public void testDupes() {
        sendCommandToServer("CREATE DATABASE " + "fred" + ";");
        sendCommandToServer("USE " + "fred" + ";");
        sendCommandToServer("CREATE TABLE " + "fred" + ";");
        String responseToDupeDatabase = sendCommandToServer("CREATE DATABASE " + "fred" + ";");
        String responseToDupeTable = sendCommandToServer("CREATE TABLE " + "fred" + ";");
        assertTrue(responseToDupeDatabase.contains("[ERROR]"), "An ERROR tag was not returned after trying to create a dupe db");
        assertTrue(responseToDupeTable.contains("[ERROR]"), "An ERROR tag was not returned after trying to create a dupe table");
    }

    @Test
    public void testValueList(){
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String incorrectPlainTextList = sendCommandToServer("CREATE TABLE marks (n--ame, m!rk, p??s);");
        String correctPlainTextList = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String incorrectValueList = sendCommandToServer("INSERT INTO marks VALUES (St1ve, 68%, YES);");
        String correctValueList = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 68, TRUE);");
        assertTrue(incorrectPlainTextList.contains("[ERROR]"), "An Error tag was not returned after entering bad values");
        assertTrue(correctPlainTextList.contains("[OK]"), "An OK tag was not returned after entering a correct plain list");
        assertTrue(incorrectValueList.contains("[ERROR]"), "AN error was not returned after entering an incorrect value list");
        assertTrue(correctValueList.contains("[OK]"), "AN OK was not returned after entering an correct value list");
    }

    @Test
    public void testAlterCreateInsertParse() {
        sendCommandToServer("CREATE DATABASE " + "fred" + ";");
        sendCommandToServer("USE " + "fred" + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("CREATE TABLE " + "fred" + ";");
        sendCommandToServer("ALTER TABLE fred ADD name");
        sendCommandToServer("ALTER TABLE fred ADD mark");
        String response = sendCommandToServer("ALTER TABLE fred ADD passed;");
        assertTrue(response.contains("[OK]"), "An OK tag was not returned after trying to insert values via alter");
    }

    @Test
    public void testAlterInsertErrors() {
        sendCommandToServer("CREATE DATABASE " + "fred" + ";");
        sendCommandToServer("USE " + "fred" + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("CREATE TABLE " + "fred" + ";");
        sendCommandToServer("ALTER TABLE fred ADD name");
        sendCommandToServer("ALTER TABLE fred ADD mark");
        String correctInsert = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String incorrectInsert = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65a, WRONG);");
        String response = sendCommandToServer("ALTER TABLE fred ADD passed;");
        assertTrue(correctInsert.contains("[OK]"), "An OK tag was not returned after trying to insert values");
        assertTrue(incorrectInsert.contains("[ERROR]"), "An ERROR tag was not returned after trying to insert incorrect values via alter");
        assertTrue(response.contains("[OK]"), "An OK tag was not returned after trying to insert values via alter");
    }

    @Test
    public void testAlterADDAndDropFunctionality() { //Drop only parses currently
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE test;");
        sendCommandToServer("ALTER TABLE test ADD marks;");
        sendCommandToServer("ALTER TABLE test ADD date;");
        sendCommandToServer("ALTER TABLE test DROP date;");
        String response = sendCommandToServer("SELECT * FROM test;");
        System.out.println("RESPONSE WAS " + response);
        assertTrue(response.contains("marks"), "An OK tag was not returned after trying to insert values via alter");
    }

    @Test
    public void testSelectparse() {
        sendCommandToServer("CREATE DATABASE " + "fred" + ";");
        sendCommandToServer("USE " + "fred" + ";");
        sendCommandToServer("CREATE TABLE " + "fred" + ";");
        String response = sendCommandToServer("SELECT " + "*" + " FROM " + "fred" + ";");
        System.out.println("ERROR MESSAGE IS " + response);
        assertTrue(response.contains("[OK]"), "An OK tag was not returned after trying to SELECT * a table");
    }

    @Test
    public void testJoinParse() {
        sendCommandToServer("CREATE DATABASE " + "fred" + ";");
        sendCommandToServer("USE " + "fred" + ";");
        sendCommandToServer("CREATE TABLE " + "fred" + ";");
        sendCommandToServer("CREATE TABLE " + "george" + ";");
        String response = sendCommandToServer("CREATE TABLE " + "fred" + ";");
        String correctJoinQuery = sendCommandToServer("JOIN " + "fred " + " AND " + "george " + " ON " + "marks " + " AND " + "tests " + ";");
        String incorrectJoinQuery = sendCommandToServer("JOIN " + "fred " + " AND " + "sam! " + " ON " + "marks " + " OR " + "tests " + ";");

        assertTrue(response.contains("[ERROR]"), "An ERROR tag was not returned after trying to CREATE a duplicate table");
        assertTrue(correctJoinQuery.contains("[OK]"), "An OK tag was not returned after joining two tables and two attribute names");
        assertTrue(incorrectJoinQuery.contains("[ERROR]"), "An ERROR tag was not returned after inputting an invalid join statement");
    }

    @Test
    public void testDatabaseandTablePersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), "Steve was added to a table and the server restarted - but Steve was not returned by SELECT *");
    }
}
