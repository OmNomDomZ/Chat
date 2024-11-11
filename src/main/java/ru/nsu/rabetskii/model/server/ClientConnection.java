package ru.nsu.rabetskii.model.server;

import ru.nsu.rabetskii.database.Database;
import ru.nsu.rabetskii.model.XmlUtility;
import ru.nsu.rabetskii.model.xmlmessage.command.*;
import ru.nsu.rabetskii.model.xmlmessage.event.Event;
import ru.nsu.rabetskii.model.xmlmessage.Success;
import ru.nsu.rabetskii.model.xmlmessage.Error;
import ru.nsu.rabetskii.model.xmlmessage.event.EventLogin;
import ru.nsu.rabetskii.model.xmlmessage.event.EventLogout;
import ru.nsu.rabetskii.model.xmlmessage.event.EventMessage;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.stream.Collectors;

public class ClientConnection extends Thread {
    private final Server server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final XmlUtility xmlUtility;
    private String userName;

    public ClientConnection(Socket socket, Server server) throws IOException {
        this.server = server;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        try {
            xmlUtility = new XmlUtility(Command.class, Event.class, Success.class, Error.class);
        } catch (JAXBException e) {
            throw new IOException("Failed to initialize JAXB", e);
        }
        start();
    }

    private void handleMessage(CommandMessage command) throws IOException, JAXBException {
        String message = command.getMessage();
        if (message == null) {
            sendErrorMessage("No message content");
            return;
        }

        server.broadcastMessage(new EventMessage(userName, message));
        server.log(userName + ": " + message);
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                int length = in.readInt();
                if (length <= 0) continue;
                byte[] buffer = new byte[length];
                in.readFully(buffer);
                String xmlMessage = new String(buffer, StandardCharsets.UTF_8);

                Object obj = xmlUtility.unmarshalFromString(xmlMessage, Object.class);

                switch (obj) {
                    case CommandLogin commandLogin -> handleLogin(commandLogin);
                    case CommandMessage commandMessage -> {
                        if (userName != null) {
                            handleMessage(commandMessage);
                        } else {
                            sendErrorMessage("Not logged in");
                        }
                    }
                    case CommandList commandList -> {
                        if (userName != null) {
                            handleList();
                        } else {
                            sendErrorMessage("Not logged in");
                        }
                    }
                    case CommandLogout commandLogout -> {
                        if (userName != null) {
                            handleLogout();
                        } else {
                            sendErrorMessage("Not logged in");
                        }
                    }
                    case null, default -> sendErrorMessage("Unknown command");
                }
            }
        } catch (EOFException e) {
            server.log("Client disconnected");
        } catch (IOException | JAXBException e) {
            server.log("Error handling client connection: " + e.getMessage());
        } finally {
            try {
                handleClientDisconnect();
                socket.close();
            } catch (IOException | JAXBException e) {
                server.log("Error closing client connection: " + e.getMessage());
            }
        }
    }

    private void handleLogin(CommandLogin command) throws IOException, JAXBException {
        String userName = command.getUserName();
        String password = command.getPassword();
        if (userName == null || password == null) {
            sendErrorMessage("Missing username or password");
            return;
        }

        String hashedPassword = server.hashPassword(password);

        if (!server.getUserPasswords().containsKey(userName)) {
            server.getUserPasswords().put(userName, hashedPassword);
            server.getActiveUsers().add(userName);
            this.userName = userName;
            sendSuccessMessage();
            server.broadcastMessage(new EventLogin(userName));
            server.log(userName + " joined the chat");
            sendHistoryMessages();
        } else if (server.getUserPasswords().get(userName).equals(hashedPassword)) {
            server.getActiveUsers().add(userName);
            this.userName = userName;
            sendSuccessMessage();
            server.broadcastMessage(new EventLogin(userName));
            server.log(userName + " joined the chat");
            sendHistoryMessages();
        } else {
            sendErrorMessage("Incorrect password");
            socket.close();
        }
    }

    private void sendHistoryMessages() throws IOException, JAXBException {
        Database database = server.getDatabase();
        synchronized (database) {
            for (String string : database.getRecentMessages()) {
                String[] parts = string.split(" ", 3);
                String event_name = parts[0];
                String username = parts[1];
                String message = parts[2];

                Event event = null;
                if (event_name.equals("message")) {
                    event = new EventMessage(username, message);
                } else if (event_name.equals("userlogin")) {
                    event = new EventLogin(username);
                } else if (event_name.equals("userlogout")) {
                    event = new EventLogout(username);
                }

                assert event != null;
                if (event instanceof EventLogin eventLogin && eventLogin.getUserName().equals(username)) {
                    continue;
                }
                sendMessageFromServer(event);
            }
        }
    }

    private void handleLogout() throws IOException, JAXBException {
        handleClientDisconnect();
        sendSuccessMessage();
    }

    private void handleClientDisconnect() throws IOException, JAXBException {
        if (userName != null) {
            server.getActiveUsers().remove(userName);
            EventLogout logoutEvent = new EventLogout(userName);
            server.broadcastMessage(logoutEvent);
            server.log(userName + " left the chat");
            userName = null;
        }
    }

    private void sendSuccessMessage() throws JAXBException, IOException {
        Success success = new Success();
        String xmlMessage = xmlUtility.marshalToXml(success);
        sendMessageFromServer(xmlMessage);
    }

    private void sendErrorMessage(String errorMessage) throws JAXBException, IOException {
        Error error = new Error(errorMessage);
        String xmlMessage = xmlUtility.marshalToXml(error);
        sendMessageFromServer(xmlMessage);
    }

    private void handleList() throws JAXBException, IOException {
        List<Success.User> users = server.getActiveUsers().stream()
                .map(name -> {
                    Success.User user = new Success.User();
                    user.setName(name);
                    return user;
                })
                .collect(Collectors.toList());

        Success success = new Success();
        Success.Users userList = new Success.Users();
        userList.setUsers(users);
        success.setUsers(userList);
        sendSuccessMessage(success);
    }

    private void sendSuccessMessage(Success success) throws JAXBException, IOException {
        String xmlMessage = xmlUtility.marshalToXml(success);
        sendMessageFromServer(xmlMessage);
    }

    private void sendMessageFromServer(String xmlMessage) throws IOException {
        byte[] messageBytes = xmlMessage.getBytes(StandardCharsets.UTF_8);
        out.writeInt(messageBytes.length);
        out.write(messageBytes);
        out.flush();
    }

    public void sendMessageFromServer(Event event) throws JAXBException, IOException {
        String xmlMessage = xmlUtility.marshalToXml(event);
        sendMessageFromServer(xmlMessage);
    }
}
