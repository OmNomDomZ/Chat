package ru.nsu.rabetskii.model.server;

import ru.nsu.rabetskii.model.XmlUtility;
import ru.nsu.rabetskii.model.xmlmessage.Command;
import ru.nsu.rabetskii.model.xmlmessage.Event;
import ru.nsu.rabetskii.model.xmlmessage.Success;
import ru.nsu.rabetskii.model.xmlmessage.Error;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.stream.Collectors;

public class ClientConnection extends Thread {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private XmlUtility xmlUtility;
    private String userName;

    public ClientConnection(Socket socket) throws IOException {
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

    private void handleMessage(Command command) throws IOException, JAXBException {
        String message = command.getMessage();
        if (message == null) {
            sendErrorMessage("No message content");
            return;
        }
        Server.broadcastMessage(new Event("message", userName, message));
        Server.log(userName + ": " + message); // Логирование отправленного сообщения
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
                Command command = xmlUtility.unmarshalFromString(xmlMessage, Command.class);

                switch (command.getCommand()) {
                    case "login":
                        handleLogin(command);
                        break;
                    case "message":
                        if (userName != null) {
                            handleMessage(command);
                        } else {
                            sendErrorMessage("Not logged in");
                        }
                        break;
                    case "list":
                        if (userName != null) {
                            handleList();
                        } else {
                            sendErrorMessage("Not logged in");
                        }
                        break;
                    case "logout":
                        if (userName != null) {
                            handleLogout(command);
                        } else {
                            sendErrorMessage("Not logged in");
                        }
                        break;
                    default:
                        sendErrorMessage("Unknown command: " + command.getCommand());
                }
            }
        } catch (EOFException e) {
            Server.log("Client disconnected");
        } catch (IOException | JAXBException e) {
            Server.log("Error handling client connection: " + e.getMessage());
        } finally {
            try {
                handleClientDisconnect();
                socket.close();
            } catch (IOException | JAXBException e) {
                Server.log("Error closing client connection: " + e.getMessage());
            }
        }
    }

    private void handleLogin(Command command) throws IOException, JAXBException {
        String userName = command.getUserName();
        String password = command.getPassword();
        if (userName == null || password == null) {
            sendErrorMessage("Missing username or password");
            return;
        }

        String hashedPassword = Server.hashPassword(password);

        if (!Server.userPasswords.containsKey(userName)) {
            Server.userPasswords.put(userName, hashedPassword);
            Server.activeUsers.add(userName);
            this.userName = userName;
            sendSuccessMessage();
            Server.broadcastMessage(new Event("userlogin", userName));
            Server.log(userName + " joined the chat");
            sendHistoryMessages();
        } else if (Server.userPasswords.get(userName).equals(hashedPassword)) {
            Server.activeUsers.add(userName);
            this.userName = userName;
            sendSuccessMessage();
            Server.broadcastMessage(new Event("userlogin", userName));
            Server.log(userName + " joined the chat");
            sendHistoryMessages();
        } else {
            sendErrorMessage("Incorrect password");
            socket.close();
        }
    }

    private void sendHistoryMessages() throws IOException, JAXBException {
        synchronized (Server.messageHistory) {
            for (Event event : Server.messageHistory) {
                if ("userlogin".equals(event.getEvent()) && event.getUserName().equals(this.userName)) {
                    continue;
                }
                sendMessage(event);
            }
        }
    }

    private void handleLogout(Command command) throws IOException, JAXBException {
        handleClientDisconnect();
        sendSuccessMessage();
    }

    private void handleClientDisconnect() throws IOException, JAXBException {
        if (userName != null) {
            Server.activeUsers.remove(userName);
            Event logoutEvent = new Event("userlogout", userName);
            Server.broadcastMessage(logoutEvent);
            Server.log(userName + " left the chat"); // Логирование отключения
            userName = null;
        }
    }

    private void sendSuccessMessage() throws JAXBException, IOException {
        Success success = new Success();
        String xmlMessage = xmlUtility.marshalToXml(success);
        sendMessage(xmlMessage);
    }

    private void sendErrorMessage(String errorMessage) throws JAXBException, IOException {
        Error error = new Error(errorMessage);
        String xmlMessage = xmlUtility.marshalToXml(error);
        sendMessage(xmlMessage);
    }

    private void handleList() throws JAXBException, IOException {
        List<Success.User> users = Server.activeUsers.stream()
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
        sendMessage(xmlMessage);
    }

    private void sendMessage(String xmlMessage) throws IOException {
        byte[] messageBytes = xmlMessage.getBytes(StandardCharsets.UTF_8);
        out.writeInt(messageBytes.length);
        out.write(messageBytes);
        out.flush();
    }

    public void sendMessage(Event event) throws JAXBException, IOException {
        String xmlMessage = xmlUtility.marshalToXml(event);
        sendMessage(xmlMessage);
    }
}
