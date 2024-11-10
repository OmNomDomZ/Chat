package ru.nsu.rabetskii.model.xmlmessage.command;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "login")
public class CommandLogin extends Command {
    private String userName;
    private String password;

    public CommandLogin() {
        super("login");
    }

    public CommandLogin(String userName, String password) {
        super("login");
        this.userName = userName;
        this.password = password;
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
}