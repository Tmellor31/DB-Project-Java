package edu.uob;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.*;

/**
 * This class implements the DB server.
 */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;
    DatabaseList databaseList;



    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();

        //server.readFile(); // Initialize table object
        server.blockingListenOn(8888);
    }

    /**
     * KEEP this signature otherwise we won't be able to mark your submission correctly.
     */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        this.databaseList = new DatabaseList();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
            //readFile();
        } catch (IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    /*public void readFile() throws IOException {
        String datafile = "databases" + File.separator + "people.tab";
        File fileToOpen = new File(datafile);

        if (fileToOpen.exists()) {
            FileReader reader = new FileReader(fileToOpen);
            BufferedReader buffReader = new BufferedReader(reader);
            String currentLine = buffReader.readLine();
            ArrayList<String> colNames = new ArrayList<>(Arrays.asList(currentLine.split("\t")));
            this.table = new Table("people", colNames);
            currentLine = buffReader.readLine();
            while (currentLine != null) {
                table.insertRow(currentLine);
                // read next line
                currentLine = buffReader.readLine();
            }
            reader.close();
        } else {
            throw new FileNotFoundException("Couldn't find people.tab");
        }
    }*/


    /**
     * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
     * able to mark your submission correctly.
     *
     * <p>This method handles all incoming DB commands and carries out the required actions.
     */
    public String handleCommand(String command) {
        Tokeniser tokeniser = new Tokeniser();
        tokeniser.query = command;
        tokeniser.setup();
        ArrayList<String> tokens = tokeniser.tokens;
        Parser parser = new Parser(tokens, databaseList);
        String response;
       try {
           response = parser.readCommand();
       }catch (Exception exception){
           return "[ERROR]" + exception;
        }

        return "[OK]" + response;
    }


    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
