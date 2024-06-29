package ru.nsu.rabetskii.model.server;

import ru.nsu.rabetskii.model.xmlmessage.Event;


import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Server {

    public static LinkedList<ClientConnection> serverList = new LinkedList<>();
    public static Map<String, String> userPasswords = new HashMap<>();
    public static Set<String> activeUsers = new HashSet<>();
    public static List<Event> messageHistory = new LinkedList<>();
    public static boolean log;

//    192.168.31.192

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Server <port>");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number.");
            System.exit(1);
            return;
        }

        try (ServerSocket server = new ServerSocket(port)) {
            Properties properties = new Properties();
            properties.load(Server.class.getResourceAsStream("/config.properties"));
            log = Boolean.parseBoolean(properties.getProperty("log"));
            log("Server Started on port " + port);
            while (true) {
                try {
                    Socket socket = server.accept();
                    ClientConnection clientConnection = new ClientConnection(socket);
                    serverList.add(clientConnection);
                } catch (IOException e) {
                    log("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log("Error starting server: " + e.getMessage());
        }
    }

    public static String hashPassword(String password) {
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

    public static void broadcastMessage(Event event) throws IOException, JAXBException {
        List<ClientConnection> toRemove = new ArrayList<>();
        for (ClientConnection client : serverList) {
            try {
                client.sendMessage(event);
            } catch (IOException e) {
                toRemove.add(client);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }
        serverList.removeAll(toRemove);
        addMessageToHistory(event);
    }

    private static void addMessageToHistory(Event event) {
        synchronized (messageHistory) {
            if (messageHistory.size() >= 10) {
                messageHistory.remove(0);
            }
            messageHistory.add(event);
        }
    }

    public static void log(String message) {
        if (log) {
            System.out.println(message);
        }
    }
}
