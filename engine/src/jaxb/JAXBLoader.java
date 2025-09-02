package jaxb;

import jaxb.engine.src.jaxb.schema.generated.*;
import exception.XMLUnmarshalException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

public class JAXBLoader {

    public core.logic.program.SProgram load(String path) {
        try {
            return unmarshalXMLFile(path);
        } catch (XMLUnmarshalException e) {
            System.err.println("Failed to process XML file: " + e.getMessage());

        }
        return null;
    }

    private core.logic.program.SProgram unmarshalXMLFile(String xmlFilePath) throws XMLUnmarshalException {
        // Check if the path ends with ".xml"
        if (!xmlFilePath.endsWith(".xml")) {
            throw new XMLUnmarshalException("File must have .xml extension: " + xmlFilePath);
        }

        core.logic.program.SProgram engineProgram;
        try {
            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.exists()) {
                throw new XMLUnmarshalException("File not found: " + xmlFilePath);
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
            engineProgram = JAXBToEngineConverter.convertJAXBToEngine(jaxbProgram);

            System.out.println("Engine Program: " + engineProgram.getName());
            System.out.println("Engine Instructions: " + engineProgram.getInstructionList().size());

        } catch (JAXBException e) {
            throw new XMLUnmarshalException("JAXB unmarshalling failed for file: " + xmlFilePath, e);
        } catch (Exception e) {
            throw new XMLUnmarshalException("Unexpected error processing file: " + xmlFilePath, e);
        }

        return engineProgram;
    }
}

