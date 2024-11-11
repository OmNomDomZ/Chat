package ru.nsu.rabetskii.model.client;

import ru.nsu.rabetskii.model.ChatObservable;
import ru.nsu.rabetskii.model.XmlUtility;
import ru.nsu.rabetskii.model.xmlmessage.command.*;
import ru.nsu.rabetskii.model.xmlmessage.event.Event;
import ru.nsu.rabetskii.model.xmlmessage.Success;
import ru.nsu.rabetskii.model.xmlmessage.Error;
import ru.nsu.rabetskii.model.xmlmessage.event.EventLogin;
import ru.nsu.rabetskii.model.xmlmessage.event.EventLogout;
import ru.nsu.rabetskii.model.xmlmessage.event.EventMessage;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final String nickname;
    private boolean isLoggedIn = false;
    private final XmlUtility xmlUtility;
    private final ChatObservable chatObservable;
    private volatile String[] userList = null;


    public ClientHandler(String addr, int port, ChatObservable chatObservable, String nickname, String password) throws IOException, JAXBException {
        this.socket = new Socket(addr, port);
        this.chatObservable = chatObservable;
        this.nickname = nickname;
        this.xmlUtility = new XmlUtility(Command.class, Event.class, Success.class, Error.class);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        promptLogin(nickname, password);
        if (isLoggedIn) {
            new ReadMsg().start();
        } else {
            downService();
        }
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    private void promptLogin(String nickname, String password) {
        try {
            Command loginCommand = new CommandLogin(nickname, password);
            sendXmlMessage(loginCommand);

            int length = in.readInt();
            if (length <= 0) {
                chatObservable.sendMessage("Error: invalid response from server");
                return;
            }
            byte[] buffer = new byte[length];
            in.readFully(buffer);
            String xmlMessage = new String(buffer, StandardCharsets.UTF_8);

            if (xmlMessage.contains("success")) {
                isLoggedIn = true;
                chatObservable.sendMessage("Login successful!");
            } else if (xmlMessage.contains("error")) {
                Error error = xmlUtility.unmarshalFromString(xmlMessage, Error.class);
                chatObservable.sendMessage("Login failed: " + error.getReason());
                isLoggedIn = false;
            }
        } catch (IOException | JAXBException e) {
            chatObservable.sendMessage("Login failed: " + e.getMessage());
            isLoggedIn = false;
        }
    }

    private void sendXmlMessage(Command command) throws JAXBException, IOException {
        String xmlMessage = xmlUtility.marshalToXml(command);
        byte[] messageBytes = xmlMessage.getBytes(StandardCharsets.UTF_8);
        out.writeInt(messageBytes.length);
        out.write(messageBytes);
        out.flush();
    }

    public void sendMessage(String message) {
        try {
            Command messageCommand = new CommandMessage(message);
            sendXmlMessage(messageCommand);
        } catch (IOException | JAXBException e) {
            chatObservable.sendMessage("Failed to send message: " + e.getMessage());
        }
    }

    public void logout() {
        try {
            Command logoutCommand = new CommandLogout();
            sendXmlMessage(logoutCommand);

            int length = in.readInt();
            if (length <= 0) {
                chatObservable.sendMessage("Error: invalid response from server");
                return;
            }
            byte[] buffer = new byte[length];
            in.readFully(buffer);
            String xmlMessage = new String(buffer, StandardCharsets.UTF_8);

            if (xmlMessage.contains("success")) {
                chatObservable.sendMessage("Logout successful!");
            } else if (xmlMessage.contains("error")) {
                Error error = xmlUtility.unmarshalFromString(xmlMessage, Error.class);
                chatObservable.sendMessage("Logout failed: " + error.getReason());
            }
        } catch (IOException | JAXBException e) {
            chatObservable.sendMessage("Failed to logout: " + e.getMessage());
        } finally {
            downService();
        }
    }

    public synchronized String[] requestUserList() {
        userList = null;
        try {
            Command listCommand = new CommandList();
            sendXmlMessage(listCommand);

            long startTime = System.currentTimeMillis();
            while (userList == null) {
                if (System.currentTimeMillis() - startTime > 5000) {
                    chatObservable.sendMessage("Timed out while waiting for user list response.");
                    return null;
                }
                Thread.sleep(100);
            }
        } catch (IOException | JAXBException | InterruptedException e) {
            chatObservable.sendMessage("Failed to request user list: " + e.getMessage());
        }
        return userList;
    }

    private void downService() {
        try {
            if (!socket.isClosed()) {
                in.close();
                out.close();
                socket.close();
            }
        } catch (IOException e) {
            chatObservable.sendMessage("Failed to close connection: " + e.getMessage());
        }
    }

    private class ReadMsg extends Thread {
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
                        case EventLogin eventLogin -> handleLogin(eventLogin);
                        case EventLogout eventLogout -> handleLogout(eventLogout);
                        case EventMessage eventMessage -> handleMessage(eventMessage);
                        case Success success -> handleSuccess(success);
                        case Error error -> chatObservable.sendMessage("Error: " + error.getReason());
                        case null, default -> System.err.println("Unknown message type received.");
                    }
                }
            } catch (IOException | JAXBException e) {
                downService();
            }
        }

        private void handleLogin(EventLogin eventLogin) {
            chatObservable.sendMessage(eventLogin.getUserName() + " joined the chat");
        }

        private void handleLogout(EventLogout eventLogout){
            chatObservable.sendMessage(eventLogout.getUserName() + " left the chat");
        }

        private void handleMessage(EventMessage eventMessage) {
            chatObservable.sendMessage(eventMessage.getFrom() + ": " + eventMessage.getMessage());
        }

        private void handleSuccess(Success success) {
            if (success.getUsers() != null) {
                userList = success.getUsers().getUsers().stream()
                        .map(Success.User::getName)
                        .toArray(String[]::new);
                chatObservable.sendMessage("\nUser list updated.");
            }
        }
    }
}
