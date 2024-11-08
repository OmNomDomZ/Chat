package ru.nsu.rabetskii.database;

import org.w3c.dom.ls.LSOutput;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Database {
    private static final String URL = "jdbc:postgresql://localhost:5432/chat_db";
    private static final String USER = "chat_user";
    private static final String PASSWORD = "chat_password";
    private Connection connection;

    public void Connect() {
        try{
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection to database established.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database: " + e.getMessage(), e);
        }
    }

    public void AddMessage(String username, String message) {
        String sql = "INSERT INTO chat_messages (username, message) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
            System.out.println("Message successfully added to the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getRecentMessages() {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT username, message FROM chat_messages ORDER BY created_at DESC LIMIT 10";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String message = resultSet.getString("message");
                messages.add(username + message);
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
                System.out.println("Connection to database closed.");
            } catch (SQLException e) {
                System.err.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }
}
