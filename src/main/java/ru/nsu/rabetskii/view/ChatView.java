package ru.nsu.rabetskii.view;

import ru.nsu.rabetskii.model.ChatModel;
import ru.nsu.rabetskii.patternobserver.Observer;
import ru.nsu.rabetskii.model.client.ClientHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatView implements Observer {
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;
    private JButton listButton;
    private ChatModel chatModel;
    private String nickname;
    private ClientHandler clientHandler;

    public ChatView(ChatModel chatModel, String nickname, ClientHandler clientHandler) {
        this.chatModel = chatModel;
        this.nickname = nickname;
        this.clientHandler = clientHandler;
        chatModel.addObserver(this);
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Chat");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(400, 300);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        textField = new JTextField(25);
        sendButton = new JButton("Send");
        listButton = new JButton("List");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
        inputPanel.add(textField);
        inputPanel.add(sendButton);
        inputPanel.add(listButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);

        sendButton.addActionListener(e -> sendMessage());

        textField.addActionListener(e -> sendMessage());

        listButton.addActionListener(e -> requestUserList());

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

    private void requestUserList() {
        clientHandler.requestUserList();
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
        textArea.append(message + "\n");
        scrollToBottom();
    }

    private void scrollToBottom() {
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
