package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.Group;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class MainXml {

    private static InputStream getInput(String project) throws IOException {
        return Resources.getResource("payload.xml").openStream();
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String projectName = sc.nextLine();

        getContentsByProjectJAXBVersion(projectName);
        getContentsByProjectStAXVersion(projectName);

    }

    private static void getContentsByProjectJAXBVersion(String projectName) throws Exception {
        JaxbParser parser = new JaxbParser(ObjectFactory.class);
        parser.setSchema(Schemas.ofClasspath("payload.xsd"));

        InputStream input = getInput(projectName);
        Payload payload = parser.unmarshal(input);
        payload.getUsers().getUser().stream().filter(u -> {
            for (Object group : u.getGroups()) {
                Group g = (Group)group;
//                if (g.getType() == GroupType.CURRENT && g.getGroupName().equals(projectName)) {
                if (g.getGroupName().equals(projectName)) {
                    return true;
                }
            }
            return false;
        }).forEach(u -> System.out.println(u.getFullName() + " " + u.getEmail()));

        input.close();
    }

    private static void getContentsByProjectStAXVersion(String projectName) throws Exception {
        InputStream input = getInput(projectName);
        try (StaxStreamProcessor processor = new StaxStreamProcessor(input)) {
            XMLStreamReader reader = processor.getReader();

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("User".equals(reader.getLocalName()) && reader.getAttributeValue(3).contains(projectName)) {

                        while (reader.hasNext()) {
                            event = reader.next();
                            if (event == XMLEvent.START_ELEMENT && "fullName".equals(reader.getLocalName())) {
                                System.out.println(reader.getElementText());
                            }
                        }
                    }
                }
            }
        }
        input.close();
    }
}
