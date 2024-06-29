package ru.nsu.rabetskii.model.client;

import ru.nsu.rabetskii.model.ChatModel;
import ru.nsu.rabetskii.model.XmlUtility;
import ru.nsu.rabetskii.model.xmlmessage.Command;
import ru.nsu.rabetskii.model.xmlmessage.Event;
import ru.nsu.rabetskii.model.xmlmessage.Success;
import ru.nsu.rabetskii.model.xmlmessage.Error;

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
    private final ChatModel chatModel;

    public ClientHandler(String addr, int port, ChatModel chatModel, String nickname, String password) throws IOException, JAXBException {
        this.socket = new Socket(addr, port);
        this.chatModel = chatModel;
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
            Command loginCommand = new Command("login", nickname, password);
            sendXmlMessage(loginCommand);

            int length = in.readInt();
            if (length <= 0) {
                chatModel.receiveMessage("Error: invalid response from server");
                return;
            }
            byte[] buffer = new byte[length];
            in.readFully(buffer);
            String xmlMessage = new String(buffer, StandardCharsets.UTF_8);

            if (xmlMessage.contains("success")) {
                isLoggedIn = true;
                chatModel.receiveMessage("Login successful!");
            } else if (xmlMessage.contains("error")) {
                Error error = xmlUtility.unmarshalFromString(xmlMessage, Error.class);
                chatModel.receiveMessage("Login failed: " + error.getMessage());
                isLoggedIn = false;
            }
        } catch (IOException | JAXBException e) {
            chatModel.receiveMessage("Login failed: " + e.getMessage());
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
            Command messageCommand = new Command("message", nickname, message);
            sendXmlMessage(messageCommand);
        } catch (IOException | JAXBException e) {
            chatModel.receiveMessage("Failed to send message: " + e.getMessage());
        }
    }

    public void logout() {
        try {
            Command logoutCommand = new Command("logout", nickname);
            sendXmlMessage(logoutCommand);

            int length = in.readInt();
            if (length <= 0) {
                chatModel.receiveMessage("Error: invalid response from server");
                return;
            }
            byte[] buffer = new byte[length];
            in.readFully(buffer);
            String xmlMessage = new String(buffer, StandardCharsets.UTF_8);

            if (xmlMessage.contains("success")) {
                chatModel.receiveMessage("Logout successful!");
            } else if (xmlMessage.contains("error")) {
                Error error = xmlUtility.unmarshalFromString(xmlMessage, Error.class);
                chatModel.receiveMessage("Logout failed: " + error.getMessage());
            }
        } catch (IOException | JAXBException e) {
            chatModel.receiveMessage("Failed to logout: " + e.getMessage());
        } finally {
            downService();
        }
    }

    public void requestUserList() {
        try {
            Command listCommand = new Command("list", nickname);
            sendXmlMessage(listCommand);
        } catch (IOException | JAXBException e) {
            chatModel.receiveMessage("Failed to request user list: " + e.getMessage());
        }
    }

    private void downService() {
        try {
            if (!socket.isClosed()) {
                in.close();
                out.close();
                socket.close();
            }
        } catch (IOException e) {
            chatModel.receiveMessage("Failed to close connection: " + e.getMessage());
        }
    }

    private class ReadMsg extends Thread {
        @Override
        public void run() {
            try {
                while (!socket.isClosed()) {
                    int length;

                    length = in.readInt();

                    if (length <= 0) continue;
                    byte[] buffer = new byte[length];
                    in.readFully(buffer);
                    String xmlMessage = new String(buffer, StandardCharsets.UTF_8);

                    if (xmlMessage.contains("event")) {
                        Event event = xmlUtility.unmarshalFromString(xmlMessage, Event.class);
                        handleEvent(event);
                    } else if (xmlMessage.contains("success")) {
                        Success success = xmlUtility.unmarshalFromString(xmlMessage, Success.class);
                        handleSuccess(success);
                    } else if (xmlMessage.contains("error")) {
                        Error error = xmlUtility.unmarshalFromString(xmlMessage, Error.class);
                        chatModel.receiveMessage("Error: " + error.getMessage());
                    }
                }
            } catch (IOException | JAXBException e) {
                ClientHandler.this.downService();
            }
        }

        private void handleEvent(Event event) {
            if ("userlogin".equals(event.getEvent())) {
                chatModel.receiveMessage(event.getUserName() + " joined the chat");
            } else if ("userlogout".equals(event.getEvent()) && !event.getUserName().equals(nickname)) {
                chatModel.receiveMessage(event.getUserName() + " left the chat");
            } else if ("message".equals(event.getEvent())) {
                chatModel.receiveMessage(event.getFrom() + ": " + event.getMessage());
            }
        }

        private void handleSuccess(Success success) {
            if (success.getUsers() != null) {
                StringBuilder userListMessage = new StringBuilder("User list:\n");
                for (Success.User user : success.getUsers().getUsers()) {
                    userListMessage.append(user.getName()).append("\n");
                }
                chatModel.receiveMessage(userListMessage.toString());
            }
        }

        private void handleClientDisconnect() {
            try {
                Command logoutCommand = new Command("logout", nickname);
                sendXmlMessage(logoutCommand);
            } catch (IOException | JAXBException e) {
                chatModel.receiveMessage("Failed to notify server about disconnection: " + e.getMessage());
            } finally {
                downService();
            }
        }
    }
}
