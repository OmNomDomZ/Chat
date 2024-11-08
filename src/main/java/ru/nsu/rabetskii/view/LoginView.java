package ru.nsu.rabetskii.view;

import ru.nsu.rabetskii.model.ChatObservable;
import ru.nsu.rabetskii.model.client.Client;
import ru.nsu.rabetskii.model.client.ClientHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView {
    private JFrame frame;
    private JTextField nicknameField;
    private JPasswordField passwordField;
    private final String addr;
    private final int port;

    public LoginView() {
        createAndShowGUI();
        Client client = new Client();
        this.addr = client.getIpAddr();
        this.port = client.getPort();
    }

    private void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("control", new Color(45, 45, 45));
            UIManager.put("info", new Color(60, 63, 65));
            UIManager.put("nimbusBase", new Color(50, 50, 50));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
            UIManager.put("nimbusLightBackground", new Color(45, 45, 45));
            UIManager.put("nimbusOrange", new Color(191, 98, 4));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
            UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
            UIManager.put("text", new Color(230, 230, 230));
        } catch (Exception e) {
            System.out.println("Failed to apply dark theme: " + e.getMessage());
        }

        frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null); // Центрируем окно на экране

        // Создание полей и кнопок
        nicknameField = new JTextField(20);
        nicknameField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        nicknameField.setOpaque(true);
        nicknameField.setBackground(new Color(60, 60, 60));
        nicknameField.setForeground(Color.WHITE);
        nicknameField.setCaretColor(Color.WHITE);

        passwordField = new JPasswordField(20);
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        passwordField.setOpaque(true);
        passwordField.setBackground(new Color(60, 60, 60));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);

        JButton loginButton = new JButton("Login");
        loginButton.setFocusPainted(false);
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Панель с полями и кнопкой
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(45, 45, 45));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nickname:"), gbc);

        gbc.gridx = 1;
        panel.add(nicknameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(loginButton, gbc);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        // Обработка события нажатия кнопки
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String nickname = nicknameField.getText();
                    String password = new String(passwordField.getPassword());
                    ChatObservable chatObservable = new ChatObservable();
                    ClientHandler clientHandler = new ClientHandler(addr, port, chatObservable, nickname, password);
                    if (clientHandler.isLoggedIn()) {
                        frame.dispose();
                        createChatView(chatObservable, nickname, clientHandler);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Login failed: " + ex.getMessage());
                }
            }
        });
    }

    private void createChatView(ChatObservable chatObservable, String nickname, ClientHandler clientHandler) {
        SwingUtilities.invokeLater(() -> new ChatView(chatObservable, nickname, clientHandler));
    }
}
