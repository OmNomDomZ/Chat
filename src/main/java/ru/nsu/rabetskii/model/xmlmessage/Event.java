package ru.nsu.rabetskii.model.xmlmessage;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "event")
public class Event {
    private String event;
    private String from;
    private String message;
    private String userName;

    public Event() {}

    public Event(String event, String from, String message) {
        this.event = event;
        this.from = from;
        this.message = message;
    }

    public Event(String event, String userName) {
        this.event = event;
        this.userName = userName;
    }

    @XmlAttribute(name = "name")
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @XmlElement(name = "from")
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @XmlElement(name = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlElement(name = "name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
