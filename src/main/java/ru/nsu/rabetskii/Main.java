//package ru.nsu.rabetskii;
//
//import ru.nsu.rabetskii.xmlmessage.Command;
//
//import javax.xml.bind.JAXBException;
//
//public class Main {
//    public static void main(String[] args) {
//        try {
//            XmlUtility utility = new XmlUtility(Command.class);
//
//            // Пример анмаршалинга
//            Command command = utility.unmarshalFromFile("src\\main\\resources\\client\\login.xml");
//            System.out.println("Command from XML: \n" + command.getCommand());
//
//            // Пример маршалинга
//            Command newCommand = new Command("logout", "user2", "4321");
//            String xmlOutput = utility.marshalToXml(newCommand);
//            System.out.println("\nXML from Object:\n" + xmlOutput);
//
//        } catch (JAXBException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}