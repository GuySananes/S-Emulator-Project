package javafxUI.service;

import core.logic.engine.Engine;
import exception.ProgramValidationException;
import exception.XMLUnmarshalException;
import javafx.concurrent.Task;
import load.LoadProgramDTO;

import java.io.File;
import java.util.Objects;

/**
 * Service responsible for loading S-Programs from XML files via the Engine singleton.
 */
public class FileLoadingService {

    private final Engine engine;

    public FileLoadingService() {
        this.engine = Engine.getInstance();
    }

    public FileLoadingService(Engine engine) {
        this.engine = Objects.requireNonNull(engine, "engine cannot be null");
    }

    /**
     * Loads a program using the Engine.
     * @param xmlFile XML file containing a program definition
     * @return LoadProgramDTO with program and context program names
     */
    public LoadProgramDTO loadProgramFromFile(File xmlFile)
            throws XMLUnmarshalException, ProgramValidationException {
        validateFile(xmlFile);

        // Call engine.loadProgram which returns LoadProgramDTO
        return engine.loadProgram(xmlFile.getAbsolutePath());
    }

    /**
     * JavaFX Task variant suitable for binding progress & message.
     */
    public Task<LoadProgramDTO> createLoadingTask(File xmlFile) {
        return new Task<LoadProgramDTO>() {
            @Override
            protected LoadProgramDTO call() throws Exception {
                updateMessage("Loading file...");
                updateProgress(0, 100);

                LoadProgramDTO result = loadProgramFromFile(xmlFile);

                updateProgress(100, 100);
                updateMessage("Complete");
                return result;
            }
        };
    }

    /**
     * Retry loop: keeps invoking engine.loadProgram until success or caller stops.
     *//*
    public PresentProgramDTO loadProgramWithRetry(java.util.function.Supplier<String> pathSupplier,
                                                  java.util.function.Function<Exception, Boolean> retryDecider)
            throws XMLUnmarshalException, ProgramValidationException {
        while (true) {
            String path = pathSupplier.get();
            if (path == null) {
                throw new XMLUnmarshalException("No path provided");
            }
            File f = new File(path);
            try {
                return loadProgramFromFile(f);
            } catch (XMLUnmarshalException | ProgramValidationException e) {
                boolean retry = retryDecider.apply(e);
                if (!retry) {
                    throw e;
                }
            }
        }
    }*/

    private void validateFile(File xmlFile) throws XMLUnmarshalException {
        if (xmlFile == null) {
            throw new XMLUnmarshalException("File is null");
        }
        if (!xmlFile.exists()) {
            throw new XMLUnmarshalException("File does not exist: " + xmlFile.getAbsolutePath());
        }
        String name = xmlFile.getName().toLowerCase();
        if (!name.endsWith(".xml")) {
            throw new XMLUnmarshalException("File must be an XML file: " + name);
        }
    }
}