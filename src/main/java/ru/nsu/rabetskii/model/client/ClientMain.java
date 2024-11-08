package ru.nsu.rabetskii.model.client;

import ru.nsu.rabetskii.view.LoginView;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginView();
        });
    }
}
