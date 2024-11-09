package ru.nsu.rabetskii.model.client;

public class Client {
    private final String ipAddr = "localhost";
    private final int port = 8080;

    public String getIpAddr() {
        return ipAddr;
    }

    public int getPort() {
        return port;
    }
}
