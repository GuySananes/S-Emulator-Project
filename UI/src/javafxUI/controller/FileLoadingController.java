package javafxUI.controller;

import core.logic.engine.Engine;
import core.logic.engine.EngineImpl;
import expand.ExpandDTO;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafxUI.model.ui.Instruction;
import javafxUI.model.ui.Program;
import javafxUI.model.ui.Variable;
import javafxUI.service.FileLoadingService;
import javafxUI.service.ModelConverter;
import present.program.PresentProgramDTO;
import run.RunProgramDTO;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Handles all file loading operations
 */
public class FileLoadingController {

    private final Program currentProgram;
    private final ObservableList<Instruction> instructions;
    private final ObservableList<Variable> variables;

    private final Button loadFileButton;
    private final TextField loadedFilePath;
    private final ProgressIndicator loadProgress;
    private final Label loadStatusLabel;

    private final Consumer<String> updateSummary;
    private final BiConsumer<String, String> showErrorDialog;

    private final FileLoadingService fileLoadingService = new FileLoadingService();

    public FileLoadingController(Program currentProgram,
                                 ObservableList<Instruction> instructions,
                                 ObservableList<Variable> variables,
                                 Button loadFileButton, TextField loadedFilePath,
                                 ProgressIndicator loadProgress, Label loadStatusLabel,
                                 Consumer<String> updateSummary,
                                 BiConsumer<String, String> showErrorDialog) {
        this.currentProgram = currentProgram;
        this.instructions = instructions;
        this.variables = variables;
        this.loadFileButton = loadFileButton;
        this.loadedFilePath = loadedFilePath;
        this.loadProgress = loadProgress;
        this.loadStatusLabel = loadStatusLabel;
        this.updateSummary = updateSummary;
        this.showErrorDialog = showErrorDialog;
    }

    public void setupEventHandlers() {
        loadFileButton.setOnAction(e -> handleLoadFile());
    }

    private void handleLoadFile() {
        File selectedFile = showFileChooser();
        if (selectedFile != null) {
            loadProgramFromFile(selectedFile);
        }
    }

    private File showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open S-Program File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        Stage stage = (Stage) loadFileButton.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    private void loadProgramFromFile(File file) {
        setLoadingState(true);
        updateSummary.accept("Loading file...");

        if (loadStatusLabel != null) loadStatusLabel.setText("Starting...");
        if (loadProgress != null) loadProgress.setProgress(-1);

        Task<PresentProgramDTO> loadingTask = fileLoadingService.createLoadingTask(file);

        if (loadProgress != null) loadProgress.progressProperty().bind(loadingTask.progressProperty());
        if (loadStatusLabel != null) loadStatusLabel.textProperty().bind(loadingTask.messageProperty());

        loadingTask.setOnSucceeded(event -> handleLoadingSuccess(file, loadingTask.getValue()));
        loadingTask.setOnFailed(event -> handleLoadingFailure(loadingTask.getException()));

        Thread loadingThread = new Thread(loadingTask);
        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    private void handleLoadingSuccess(File file, PresentProgramDTO dto) {
        unbindProgress();

        try {
            updateUIWithLoadedDto(file, dto);
            if (loadProgress != null) loadProgress.setProgress(1);
            if (loadStatusLabel != null) loadStatusLabel.setText("Loaded");
        } catch (Exception e) {
            showErrorDialog.accept("Processing Error", "Error processing loaded program: " + e.getMessage());
            updateSummary.accept("Error processing file: " + e.getMessage());
            if (loadProgress != null) loadProgress.setProgress(0);
            if (loadStatusLabel != null) loadStatusLabel.setText("Process error");
        } finally {
            setLoadingState(false);
        }
    }

    private void handleLoadingFailure(Throwable exception) {
        unbindProgress();
        showErrorDialog.accept("Loading Error", "Failed to load file: " + exception.getMessage());
        updateSummary.accept("Failed to load file: " + exception.getMessage());
        if (loadProgress != null) loadProgress.setProgress(0);
        if (loadStatusLabel != null) loadStatusLabel.setText("Failed");
        setLoadingState(false);
    }

    private void unbindProgress() {
        if (loadProgress != null) loadProgress.progressProperty().unbind();
        if (loadStatusLabel != null) loadStatusLabel.textProperty().unbind();
    }

    private void updateUIWithLoadedDto(File file, PresentProgramDTO dto) {
        Program uiProgram = ModelConverter.convertProgram(dto);

        currentProgram.setFilePath(file.getAbsolutePath());
        currentProgram.setName(uiProgram.getName());
        currentProgram.setLoaded(true);
        currentProgram.setTotalCycles(uiProgram.getTotalCycles());

        // Get degree information using DTOs only
        try {
            Engine engine = EngineImpl.getInstance();

            // Primary strategy: Use RunProgramDTO for degrees
            RunProgramDTO runDTO = engine.runProgram();
            currentProgram.setMaxDegree(runDTO.getMaxDegree());
            currentProgram.setMinDegree(runDTO.getMinDegree());
            currentProgram.setCurrentDegree(0); // Always start at degree 0

        } catch (Exception e) {
            // Fallback: Use ExpandDTO for degrees
            try {
                Engine engine = EngineImpl.getInstance();
                ExpandDTO expandDTO = engine.expandProgram();
                currentProgram.setMaxDegree(expandDTO.getMaxDegree());
                currentProgram.setMinDegree(expandDTO.getMinDegree());
                currentProgram.setCurrentDegree(0);
            } catch (Exception fallbackException) {
                // Final fallback to defaults
                currentProgram.setMaxDegree(0);
                currentProgram.setMinDegree(0);
                currentProgram.setCurrentDegree(0);
                updateSummary.accept("Warning: Could not determine program expansion degrees");
            }
        }

        updateUICollections(dto);
        updateSummary.accept("File loaded successfully: " + file.getName() +
                " (Max degree: " + currentProgram.getMaxDegree() + ")");
    }

    private void updateUICollections(PresentProgramDTO dto) {
        instructions.clear();
        variables.clear();
        currentProgram.clearInstructions();
        currentProgram.clearVariables();

        List<Instruction> convertedInstructions = ModelConverter.convertInstructions(dto);
        instructions.addAll(convertedInstructions);
        convertedInstructions.forEach(currentProgram::addInstruction);

        List<Variable> convertedVariables = ModelConverter.convertVariables(dto);
        variables.addAll(convertedVariables);
        convertedVariables.forEach(currentProgram::addVariable);
    }

    private void setLoadingState(boolean loading) {
        loadFileButton.setDisable(loading);
    }
}