package javafxUI.service;

import core.logic.engine.Engine;
import core.logic.engine.EngineImpl;
import present.program.PresentProgramDTO;
import exception.XMLUnmarshalException;
import exception.ProgramValidationException;
import exception.NoProgramException;
import javafx.concurrent.Task;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for loading S-Programs from XML files via the Engine singleton.
 * Mirrors the consoleUI flow: validate path -> engine.loadProgram(path) -> obtain present DTO.
 */
public class FileLoadingService {

    private final Engine engine;

    public FileLoadingService() {
        this.engine = EngineImpl.getInstance();
    }

    public FileLoadingService(Engine engine) {
        this.engine = Objects.requireNonNull(engine, "engine cannot be null");
    }

    /**
     * Loads a program (single attempt) using the Engine just like the console UI.
     * @param xmlFile XML file containing a program definition
     * @return PresentProgramDTO ready for UI consumption
     */
    public PresentProgramDTO loadProgramFromFile(File xmlFile)
            throws XMLUnmarshalException, ProgramValidationException {
        validateFile(xmlFile);
        engine.loadProgram(xmlFile.getAbsolutePath());
        try {
            return engine.presentProgram();
        } catch (NoProgramException e) {
            // This should not happen immediately after a successful load; wrap as runtime.
            throw new IllegalStateException("Program unexpectedly unavailable after load", e);
        }
    }

    /**
     * Asynchronous load (single attempt) using a background thread.
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
     * JavaFX Task variant (single attempt) suitable for binding progress & message.
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
     * Retry loop similar to console UI: keeps invoking engine.loadProgram until success or caller stops.
     * @param pathSupplier supplies a new path each retry (e.g., from a dialog). Must not return null for retry attempts.
     * @param retryDecider given the exception, return true to retry, false to propagate.
     * @return PresentProgramDTO on success
     */
    public PresentProgramDTO loadProgramWithRetry(java.util.function.Supplier<String> pathSupplier,
                                                  java.util.function.Function<Exception, Boolean> retryDecider)
            throws XMLUnmarshalException, ProgramValidationException {
        int attempt = 0;
        while (true) {
            attempt++;
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
