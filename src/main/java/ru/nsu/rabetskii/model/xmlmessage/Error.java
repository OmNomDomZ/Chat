package ru.nsu.rabetskii.model.xmlmessage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public class Error {
    private String reason;

    public Error() {}

    public Error(String reason) {
        this.reason = reason;
    }

    @XmlElement(name = "message")
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
