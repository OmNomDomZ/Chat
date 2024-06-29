package ru.nsu.rabetskii.model.xmlmessage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public class Error {
    private String message;

    public Error() {}

    public Error(String message) {
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
