package ru.javaops.masterjava.xml.util;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MainXml {
    private static InputStream input;

    static {
        try {
            input = Resources.getResource("payload.xml").openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String projectName = sc.nextLine();

        getContentsByProjectJAXBVersion(projectName).forEach(u -> System.out.println(u.getFullName() + " " + u.getEmail()));

        input.close();
    }

    private static List<User> getContentsByProjectJAXBVersion(String projectName) throws JAXBException {
        JaxbParser parser = new JaxbParser(ObjectFactory.class);
        parser.setSchema(Schemas.ofClasspath("payload.xsd"));

        Payload payload = parser.unmarshal(input);
        return payload.getUsers().getUser().stream().filter(u -> {
            for (Object group : u.getGroups()) {
                Group g = (Group)group;
//                if (g.getType() == GroupType.CURRENT && g.getGroupName().equals(projectName)) {
                if (g.getGroupName().equals(projectName)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }
}
