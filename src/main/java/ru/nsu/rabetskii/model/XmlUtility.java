package ru.nsu.rabetskii.model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtility {

    private final JAXBContext jaxbContext;
    private final Marshaller marshaller;
    private final Unmarshaller unmarshaller;

    public XmlUtility(Class<?>... classes) throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(classes);
        this.marshaller = jaxbContext.createMarshaller();
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public <T> T unmarshalFromString(String xml, Class<T> clazz) throws JAXBException {
        synchronized (unmarshaller) {
            return clazz.cast(unmarshaller.unmarshal(new StringReader(xml)));
        }
    }

    public String marshalToXml(Object obj) throws JAXBException {
        synchronized (marshaller) {
            StringWriter writer = new StringWriter();
//            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(obj, writer);
            return writer.toString();
        }
    }
}
