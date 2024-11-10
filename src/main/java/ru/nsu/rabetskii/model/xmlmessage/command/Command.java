package ru.nsu.rabetskii.model.xmlmessage.command;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({CommandLogin.class, CommandMessage.class, CommandLogout.class, CommandList.class})
@XmlRootElement(name = "command")
public abstract class Command {
    private String command;

    public Command() {}

    public Command(String command) {
        this.command = command;
    }

    @XmlAttribute(name = "name")
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}