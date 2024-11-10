package ru.nsu.rabetskii.model.xmlmessage.command;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "list")
public class CommandList extends Command {
    public CommandList() {
        super("list");
    }
}