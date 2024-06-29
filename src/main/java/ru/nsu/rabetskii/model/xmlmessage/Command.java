package ru.nsu.rabetskii.model.xmlmessage;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "command")
public class Command {
    private String command;
    private String userName;
    private String password;
    private String message;

    public Command() {}

    public Command(String command, String userName) {
        this.command = command;
        this.userName = userName;
    }

    public Command(String command, String userName, String action) {
        this.command = command;
        this.userName = userName;
        if ("login".equals(command)) {
            this.password = action;
        } else if ("message".equals(command)) {
            this.message = action;
        }
    }

    @XmlAttribute(name = "name")
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @XmlElement(name = "name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @XmlElement(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
