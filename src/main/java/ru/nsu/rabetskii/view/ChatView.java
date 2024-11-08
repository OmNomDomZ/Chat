package ru.nsu.rabetskii.view;

import ru.nsu.rabetskii.model.ChatObservable;
import ru.nsu.rabetskii.patternobserver.Observer;
import ru.nsu.rabetskii.model.client.ClientHandler;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatView implements Observer {
    private JFrame frame;
    private JTextPane textPane;
    private JTextField textField;
    private final String nickname;
    private final ClientHandler clientHandler;

    public ChatView(ChatObservable chatObservable, String nickname, ClientHandler clientHandler) {
        this.nickname = nickname;
        this.clientHandler = clientHandler;
        chatObservable.addObserver(this);
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame(nickname);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(new Color(60, 60, 60));
        textPane.setForeground(Color.WHITE);
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textPane);

        textField = new JTextField(25);
        textField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        textField.setOpaque(true);
        textField.setBackground(new Color(70, 70, 70));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton sendButton = new JButton("Send");
        sendButton.setFocusPainted(false);
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton listButton = new JButton("List");
        listButton.setFocusPainted(false);
        listButton.setBackground(new Color(100, 100, 180));
        listButton.setForeground(Color.WHITE);
        listButton.setFont(new Font("Arial", Font.BOLD, 14));
        listButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 45, 45));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
        inputPanel.setBackground(new Color(45, 45, 45));
        inputPanel.add(textField);
        inputPanel.add(sendButton);
        inputPanel.add(listButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);

        sendButton.addActionListener(e -> sendMessage());

        textField.addActionListener(e -> sendMessage());

        listButton.addActionListener(e -> showUserList());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLogout();
            }
        });
    }

    private void sendMessage() {
        String message = textField.getText();
        if (!message.trim().isEmpty()) {
            clientHandler.sendMessage(message);
            textField.setText("");
        }
    }

    private void showUserList() {
        String[] users = clientHandler.requestUserList();
        if (users != null && users.length > 0) {
            appendStyledText("\n=== User List ===\n", Color.CYAN);
            for (String user : users) {
                appendStyledText("| " + user + "\n", new Color(4, 174, 4));
            }
            appendStyledText("=================\n", Color.CYAN);
        } else {
            appendStyledText("\n[INFO]: No users found or failed to retrieve user list.\n", Color.RED);
        }
    }

    private void appendStyledText(String text, Color color) {
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Style", null);
        StyleConstants.setForeground(style, color);
        try {
            doc.insertString(doc.getLength(), text, style);
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        try {
            clientHandler.logout();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            frame.dispose();
        }
    }

    @Override
    public void update(String message) {
        appendStyledText(message + "\n", Color.WHITE);
    }
}
