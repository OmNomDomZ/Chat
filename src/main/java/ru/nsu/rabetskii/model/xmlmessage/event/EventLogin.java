package ru.nsu.rabetskii.model.xmlmessage.event;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userlogin")
public class EventLogin extends Event {
    private String userName;

    public EventLogin() {
        super("userlogin");
    }

    public EventLogin(String userName) {
        super("userlogin");
        this.userName = userName;
    }

    @XmlElement(name = "name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
