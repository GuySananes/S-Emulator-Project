package javafx.service;

import jaxb.JAXBLoader;
import core.logic.program.SProgram;
import exception.XMLUnmarshalException;
import exception.ProgramValidationException;
import javafx.concurrent.Task;
import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for loading S-Programs from XML files
 */
public class FileLoadingService {

    private final JAXBLoader jaxbLoader;

    public FileLoadingService() {
        this.jaxbLoader = new JAXBLoader();
    }

    /**
     * Loads a program from XML file synchronously
     */
    public SProgram loadProgramFromFile(File xmlFile) throws XMLUnmarshalException, ProgramValidationException {
        if (xmlFile == null || !xmlFile.exists()) {
            throw new XMLUnmarshalException("File does not exist or is null");
        }

        if (!xmlFile.getName().toLowerCase().endsWith(".xml")) {
            throw new XMLUnmarshalException("File must be an XML file");
        }

        return jaxbLoader.load(xmlFile.getAbsolutePath());
    }

    /**
     * Loads a program from XML file asynchronously using CompletableFuture
     */
    public CompletableFuture<SProgram> loadProgramAsync(File xmlFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadProgramFromFile(xmlFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Creates a JavaFX Task for loading a program (useful for progress monitoring)
     */
    public Task<SProgram> createLoadingTask(File xmlFile) {
        return new Task<SProgram>() {
            @Override
            protected SProgram call() throws Exception {
                updateMessage("Loading file: " + xmlFile.getName());
                updateProgress(0, 1);

                SProgram program = loadProgramFromFile(xmlFile);

                updateMessage("File loaded successfully");
                updateProgress(1, 1);

                return program;
            }
        };
    }
}
