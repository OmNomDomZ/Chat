package ru.nsu.rabetskii.model.client;

import ru.nsu.rabetskii.model.ChatModel;
import ru.nsu.rabetskii.view.LoginView;

import javax.swing.*;

public class Client {

//    public static String ipAddr = "localhost";
//    public static int port = 8080;
//    public static String ipAddr = "45.142.36.163";
//    public static int port = 8000;

    public static String ipAddr = "192.168.31.85";
    public static int port = 8886;

    public static void main(String[] args) {
        ChatModel chatModel = new ChatModel();
        SwingUtilities.invokeLater(() ->{
            new LoginView(chatModel, ipAddr, port);
        });
    }
}
