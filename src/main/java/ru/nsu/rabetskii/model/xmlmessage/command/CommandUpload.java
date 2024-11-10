package ru.nsu.rabetskii.model.xmlmessage.command;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "upload")
public class CommandUpload extends Command {
    private String fileName;
    private String fileType;
    private String encoding;
    private String content;

    public CommandUpload() {
        super("upload");
    }

    public CommandUpload(String fileName, String fileType, String encoding, String content) {
        super("upload");
        this.fileName = fileName;
        this.fileType = fileType;
        this.encoding = encoding;
        this.content = content;
    }

    @XmlElement(name = "name")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @XmlElement(name = "mimeType")
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @XmlElement(name = "encoding")
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @XmlElement(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
