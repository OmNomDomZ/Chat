package ru.nsu.rabetskii.model.xmlmessage.event;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({EventLogin.class, EventLogout.class, EventMessage.class})
public abstract class Event {
    private String event;

    public Event() {}

    public Event(String event) {
        this.event = event;
    }

    @XmlAttribute(name = "name")
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
