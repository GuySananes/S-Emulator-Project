package jaxb;


import jaxb.engine.src.jaxb.schema.generated.*;
import exception.XMLUnmarshalException;
import exception.ProgramValidationException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

public class JAXBLoader {

    public core.logic.program.SProgram load(String path) throws XMLUnmarshalException, ProgramValidationException {
        return unmarshalXMLFile(path);
    }

    private core.logic.program.SProgram unmarshalXMLFile(String xmlFilePath) throws XMLUnmarshalException, ProgramValidationException {
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

            //System.out.println("Processing: " + xmlFilePath);

            // JAXB will automatically create the objects when unmarshalling
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            jaxb.engine.src.jaxb.schema.generated.SProgram jaxbProgram = (jaxb.engine.src.jaxb.schema.generated.SProgram) unmarshaller
                    .unmarshal(xmlFile);

            //System.out.println("JAXB Program: " + jaxbProgram.getName());
            //System.out.println("JAXB Instructions: " + jaxbProgram.getSInstructions().getSInstruction().size());

            // Convert JAXB objects to real engine objects (may throw ProgramValidationException)
            engineProgram = JAXBToEngineConverter.convertJAXBToEngine(jaxbProgram);

            //System.out.println("Engine Program: " + engineProgram.getName());
            //System.out.println("Engine Instructions: " + engineProgram.getInstructionList().size());

        } catch (JAXBException e) {
            throw new XMLUnmarshalException("JAXB unmarshalling failed for file: " + xmlFilePath, e);
        } catch (XMLUnmarshalException e) {
            // Rethrow as-is to preserve specific cause (file not found, bad extension)
            throw e;
        } catch (ProgramValidationException e) {
            // Propagate validation failures
            throw e;
        } catch (Exception e) {
            throw new XMLUnmarshalException("Unexpected error processing file: " + xmlFilePath, e);
        }

        return engineProgram;
    }
}
