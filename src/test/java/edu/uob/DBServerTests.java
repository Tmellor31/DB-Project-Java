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

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
                    return server.handleCommand(command);
                },
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

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

}
