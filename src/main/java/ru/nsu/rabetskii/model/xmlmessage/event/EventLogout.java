package ru.nsu.rabetskii.model.xmlmessage.event;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userlogout")
public class EventLogout extends Event {
    private String userName;

    public EventLogout() {
        super("userlogout");
    }

    public EventLogout(String userName) {
        super("userlogout");
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
