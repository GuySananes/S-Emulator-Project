package javafxUI.service;

import core.logic.engine.Engine;
import present.program.PresentProgramDTO;
import load.LoadProgramDTO;
import exception.XMLUnmarshalException;
import exception.ProgramValidationException;
import javafx.concurrent.Task;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
     * @return PresentProgramDTO ready for UI consumption
     */
    public PresentProgramDTO loadProgramFromFile(File xmlFile)
            throws XMLUnmarshalException, ProgramValidationException {
        validateFile(xmlFile);
        LoadProgramDTO loadResult = engine.loadProgram(xmlFile.getAbsolutePath());
        return loadResult.getPresentProgramDTO();
    }

    /**
     * Asynchronous load using a background thread.
     */
    public CompletableFuture<PresentProgramDTO> loadProgramAsync(File xmlFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadProgramFromFile(xmlFile);
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * JavaFX Task variant suitable for binding progress & message.
     */
    public Task<PresentProgramDTO> createLoadingTask(File xmlFile) {
        return new Task<>() {
            @Override
            protected PresentProgramDTO call() throws Exception {
                updateMessage("Loading file: " + (xmlFile != null ? xmlFile.getName() : "<null>"));
                updateProgress(0, 1);
                PresentProgramDTO dto = loadProgramFromFile(xmlFile);
                updateMessage("Program loaded successfully");
                updateProgress(1, 1);
                return dto;
            }
        };
    }

    /**
     * Retry loop: keeps invoking engine.loadProgram until success or caller stops.
     */
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
    }

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