package jaxb;

import jaxb.engine.src.jaxb.schema.generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

public class JAXBMain {

    public static void main(String[] args) {
       /* if (args.length > 0) {
            unmarshalXMLFile(args[0]);
        } else {
            System.out.println("Usage: java JAXBMain <xml-file>");
        }*/
        unmarshalXMLFile("C:\\Users\\guysa\\Java course 25\\S-Emulator-Project\\engine\\src\\resourses\\successor.xml");
        unmarshalXMLFile("C:\\Users\\guysa\\Java course 25\\S-Emulator-Project\\engine\\src\\resourses\\synthetic.xml");
    }

    private static void unmarshalXMLFile(String xmlFilePath) {
        try {
            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.exists()) {
                System.err.println("File not found: " + xmlFilePath);
                return;
            }

            System.out.println("Processing: " + xmlFilePath);

            // JAXB will automatically create the objects when unmarshalling
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            jaxb.engine.src.jaxb.schema.generated.SProgram jaxbProgram = (jaxb.engine.src.jaxb.schema.generated.SProgram) unmarshaller
                    .unmarshal(xmlFile);

            System.out.println("JAXB Program: " + jaxbProgram.getName());
            System.out.println("JAXB Instructions: " + jaxbProgram.getSInstructions().getSInstruction().size());

            // Convert JAXB objects to real engine objects
            core.logic.program.SProgram engineProgram = JAXBToEngineConverter.convertJAXBToEngine(jaxbProgram);

            System.out.println("Engine Program: " + engineProgram.getName());
            System.out.println("Engine Instructions: " + engineProgram.getInstructionList().size());

        } catch (JAXBException e) {
            System.err.println("JAXB Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
