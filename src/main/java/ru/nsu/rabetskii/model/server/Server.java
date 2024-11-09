package ru.nsu.rabetskii.model.server;

import ru.nsu.rabetskii.database.Database;
import ru.nsu.rabetskii.model.xmlmessage.Event;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Server {
    private final LinkedList<ClientConnection> serverList = new LinkedList<>();
    private final Map<String, String> userPasswords = new HashMap<>();
    private final Set<String> activeUsers = new HashSet<>();
    private boolean log;
    private final Database database = new Database();

    public void startServer(int port) {

        try (ServerSocket server = new ServerSocket(port)) {
            Properties properties = new Properties();
            properties.load(Server.class.getResourceAsStream("/config.properties"));
            log = Boolean.parseBoolean(properties.getProperty("log"));
            log("Server Started on port " + port);
            database.Connect(log);
            while (true) {
                try {
                    Socket socket = server.accept();
                    ClientConnection clientConnection = new ClientConnection(socket, this);
                    serverList.add(clientConnection);
                } catch (IOException e) {
                    log("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log("Error starting server: " + e.getMessage());
        }
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcastMessage(Event event){
        List<ClientConnection> toRemove = new ArrayList<>();
        for (ClientConnection client : serverList) {
            try {
                client.sendMessageFromServer(event);
            } catch (IOException e) {
                toRemove.add(client);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        serverList.removeAll(toRemove);
        addMessageToHistory(event);
    }

    private void addMessageToHistory(Event event) {
        synchronized (database) {
            if (event.getEvent().equals("userlogin") || event.getEvent().equals("userlogout")) {
                database.AddMessage(event.getEvent(), event.getUserName(), event.getMessage());
            } else if (event.getEvent().equals("message")) {
                database.AddMessage(event.getEvent(), event.getFrom(), event.getMessage());
            }
        }
    }

    public void log(String message) {
        if (log) {
            System.out.println(message);
        }
    }

    public Map<String, String> getUserPasswords() {
        return userPasswords;
    }

    public Set<String> getActiveUsers() {
        return activeUsers;
    }

    public Database getDatabase() {
        return database;
    }
}
