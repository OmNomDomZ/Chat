package ru.nsu.rabetskii.model.xmlmessage.command;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "logout")
public class CommandLogout extends Command {
    public CommandLogout() {
        super("logout");
    }
}