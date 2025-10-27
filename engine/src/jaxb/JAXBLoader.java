package jaxb;

import exception.ProgramValidationException;
import exception.XMLUnmarshalException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jaxb.engine.src.jaxb.schema.generated.SProgram;

import java.io.File;

public class JAXBLoader {

    public core.logic.program.SProgram load(String path) throws XMLUnmarshalException, ProgramValidationException {
        return load(path, null);
    }

    public core.logic.program.SProgram load(String path, java.util.Set<String> systemFunctionNames)
            throws XMLUnmarshalException, ProgramValidationException {
        return unmarshalXMLFile(path, systemFunctionNames);
    }

    private core.logic.program.SProgram unmarshalXMLFile(String xmlFilePath, java.util.Set<String> systemFunctionNames)
            throws XMLUnmarshalException, ProgramValidationException {
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

            // JAXB will automatically create the objects when unmarshalling
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            jaxb.engine.src.jaxb.schema.generated.SProgram jaxbProgram = (jaxb.engine.src.jaxb.schema.generated.SProgram) unmarshaller
                    .unmarshal(xmlFile);

            // Convert JAXB objects to real engine objects with system function context
            engineProgram = JAXBToEngineConverter.convertJAXBToEngine(jaxbProgram, systemFunctionNames);

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