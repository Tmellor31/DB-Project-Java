package edu.uob;

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
    public Table table;

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.readFile(); // Initialize table object
        server.blockingListenOn(8888);
    }

    /**
     * KEEP this signature otherwise we won't be able to mark your submission correctly.
     */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
            readFile();
            printFile();
        } catch (IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    public void readFile() throws IOException {
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
    }

    public void printFile() {
        System.out.println(table.getName());
        for (Column col : table.getColumns()) {
            System.out.print(col.getName() + "\t");
        }
        System.out.println();
        for (Row row : table.getRows()) {
            for (String value : row.getValues().values()) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
    }


    /**
     * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
     * able to mark your submission correctly.
     *
     * <p>This method handles all incoming DB commands and carries out the required actions.
     */
    public String handleCommand(String command) {
        Tokeniser tokeniser = new Tokeniser();
        tokeniser.tokens.add(command);
        tokeniser.setup();
        tokeniser.tokenise(command);

        StringBuilder result = new StringBuilder();

        // Display all rows and columns
        for (Column col : table.getColumns()) {
            result.append(col.getName()).append(": ");
            for (Row row : table.getRows()) {
                result.append(row.getValues().get(col.getName())).append("\t");
            }
            result.append("\n");
        }

        return result.toString();
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
