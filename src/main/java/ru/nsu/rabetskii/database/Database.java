package ru.nsu.rabetskii.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Database {
    private final String URL = "jdbc:postgresql://localhost:5432/chat_db";
    private final String USER = "chat_user";
    private final String PASSWORD = "chat_password";
    private Connection connection;
    private boolean log;

    public void Connect(boolean log) {
        try{
            this.log = log;
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            String sql = "TRUNCATE TABLE chat_messages RESTART IDENTITY;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
            if (log){
                System.out.println("Connection to database established.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database: " + e.getMessage(), e);
        }
    }

    public void AddMessage(String event, String username, String message) {
        String sql = "INSERT INTO chat_messages (event, username, message) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, event);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, message);
            preparedStatement.executeUpdate();
            if (log){
                System.out.println("Message successfully added to the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getRecentMessages() {
        List<String> messages = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        String sql = "SELECT event, username, message FROM chat_messages ORDER BY created_at DESC LIMIT 10";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String event = resultSet.getString("event");
                String username = resultSet.getString("username");
                String message = resultSet.getString("message");
                temp.add(event + ' ' + username + ' ' + message);
            }

            for (int i = temp.size() - 1; i >= 0; i--) {
                messages.add(temp.get(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }


    public void close() {
        if (connection != null) {
            try {
                connection.close();
                if (log){
                    System.out.println("Connection to database closed.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }
}
