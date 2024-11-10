package ru.nsu.rabetskii.model.xmlmessage.command;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "message")
public class CommandMessage extends Command {
    private String message;

    public CommandMessage() {
        super("message");
    }

    public CommandMessage(String message) {
        super("message");
        this.message = message;
    }

    @XmlElement(name = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}