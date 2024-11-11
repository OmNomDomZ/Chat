package ru.nsu.rabetskii.model.xmlmessage.event;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EventMessage")
public class EventMessage extends Event {
    private String from;
    private String message;

    public EventMessage() {
        super("message");
    }

    public EventMessage(String from, String message) {
        super("message");
        this.from = from;
        this.message = message;
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
}
