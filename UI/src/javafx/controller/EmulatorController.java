package javafx.controller;

import core.logic.engine.Engine;
import core.logic.engine.EngineImpl;
import core.logic.program.SProgram;
import exception.NoProgramException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.controller.dialog.InputDialog;
import javafx.fxml.FXML;
import javafx.model.ui.*;
import javafx.scene.control.*;
import javafx.service.FileLoadingService;
import javafx.service.ModelConverter;
import javafx.service.ProgramExecutionService;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import present.PresentProgramDTO;
import run.RunProgramDTO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EmulatorController {

    // FXML UI Components
    @FXML private Button loadFileButton;
    @FXML private TextField loadedFilePath;
    @FXML private ComboBox<String> programSelector;
    @FXML private Button collapseButton;
    @FXML private Label currentDegreeLabel;
    @FXML private Button expandButton;
    @FXML private Button highlightButton;
    @FXML private TableView<Instruction> instructionsTable;
    @FXML private Label summaryLine;
    @FXML private TextArea historyChain;
    @FXML private Button startRegularButton;
    @FXML private Button startDebugButton;
    @FXML private Button stopButton;
    @FXML private Button resumeButton;
    @FXML private Button stepOverButton;
    @FXML private Button stepBackButton;
    @FXML private TableView<Variable> variablesTable;
    @FXML private TextArea executionInputs;
    @FXML private Label cyclesLabel;
    @FXML private TableView<Statistic> statisticsTable;
    @FXML private Button showStatsButton;
    @FXML private Button rerunButton;
    @FXML private ProgressIndicator loadProgress; // added
    @FXML private Label loadStatusLabel; // added

    // Model objects
    private final Program currentProgram = new Program();
    private final ExecutionResult executionResult = new ExecutionResult();
    private final ObservableList<Instruction> instructions = FXCollections.observableArrayList();
    private final ObservableList<Variable> variables = FXCollections.observableArrayList();
    private final ObservableList<Statistic> statistics = FXCollections.observableArrayList();
    private final ObservableList<SLabel> labels = FXCollections.observableArrayList();

    // Service classes (separation of concerns)
    private final FileLoadingService fileLoadingService = new FileLoadingService();
    private final ProgramExecutionService executionService = new ProgramExecutionService();
    private SProgram loadedEngineProgram;

    // This method is automatically called after the fxml file has been loaded
    @FXML
    public void initialize() {
        setupTables();
        setupEventHandlers();
        setupDataBinding();
    }

    private void setupDataBinding() {
        // Bind program data to UI components
        currentDegreeLabel.textProperty().bind(
            currentProgram.minDegreeProperty().asString()
            .concat(" / ")
            .concat(currentProgram.maxDegreeProperty().asString())
        );

        // Bind execution result to cycles label
        cyclesLabel.textProperty().bind(executionResult.cyclesProperty().asString());

        // Bind execution history to history chain
        executionResult.getExecutionHistory().addListener((javafx.collections.ListChangeListener<String>) change -> {
            StringBuilder history = new StringBuilder();
            for (String step : executionResult.getExecutionHistory()) {
                history.append(step).append("\n");
            }
            historyChain.setText(history.toString());
        });

        // Bind program name to loaded file path (when no file is selected)
        loadedFilePath.textProperty().bind(
            javafx.beans.binding.Bindings.when(currentProgram.filePathProperty().isNotEmpty())
                .then(currentProgram.filePathProperty())
                .otherwise("No file loaded")
        );
    }

    private void setupTables() {
        // Setup Instructions Table
        TableColumn<Instruction, Number> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());

        TableColumn<Instruction, String> typeCol = new TableColumn<>("B\\S");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());

        TableColumn<Instruction, Number> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());

        TableColumn<Instruction, String> descCol = new TableColumn<>("Instruction");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        instructionsTable.getColumns().setAll(numberCol, typeCol, cyclesCol, descCol);
        instructionsTable.setItems(instructions);

        // Make instructions table 3 times taller
        instructionsTable.setPrefHeight(450); // Increased from default ~150 to 450
        instructionsTable.setMinHeight(450);

        // Setup Variables Table
        TableColumn<Variable, String> varNameCol = new TableColumn<>("Name");
        varNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Variable, Number> varValueCol = new TableColumn<>("Value");
        varValueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty());

        variablesTable.getColumns().setAll(varNameCol, varValueCol);
        variablesTable.setItems(variables);

        // Setup Statistics Table
        TableColumn<Statistic, String> execTypeCol = new TableColumn<>("Execution Type");
        execTypeCol.setCellValueFactory(cellData -> cellData.getValue().executionTypeProperty());

        TableColumn<Statistic, Number> cyclesStatsCol = new TableColumn<>("Total Cycles");
        cyclesStatsCol.setCellValueFactory(cellData -> cellData.getValue().totalCyclesProperty());

        statisticsTable.getColumns().setAll(execTypeCol, cyclesStatsCol);
        statisticsTable.setItems(statistics);
    }

    private void setupEventHandlers() {
        // File loading
        loadFileButton.setOnAction(e -> handleLoadFile());

        // Program selector
        programSelector.setOnAction(e -> handleProgramSelection());

        // Program controls
        collapseButton.setOnAction(e -> handleCollapse());
        expandButton.setOnAction(e -> handleExpand());
        highlightButton.setOnAction(e -> handleHighlight());

        // Debugger controls
        startRegularButton.setOnAction(e -> handleStartRegular());
        startDebugButton.setOnAction(e -> handleStartDebug());
        stopButton.setOnAction(e -> handleStop());
        resumeButton.setOnAction(e -> handleResume());
        stepOverButton.setOnAction(e -> handleStepOver());
        stepBackButton.setOnAction(e -> handleStepBack());

        // Statistics controls
        showStatsButton.setOnAction(e -> handleShowStats());
        rerunButton.setOnAction(e -> handleRerun());
    }

    private void handleProgramSelection() {
        String selectedProgram = programSelector.getValue();
        if (selectedProgram != null) {
            // TODO: Switch to selected program/function
            currentProgram.setName(selectedProgram);
            updateSummary("Switched to program: " + selectedProgram);
        }
    }

    // Event handler methods
    private void handleLoadFile() {
        File selectedFile = showFileChooser();
        if (selectedFile != null) {
            loadProgramFromFile(selectedFile);
        }
    }

    /**
     * Shows file chooser dialog (UI logic only)
     */
    private File showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open S-Program File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        Stage stage = (Stage) loadFileButton.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Loads program using service and updates UI (delegates to service)
     */
    private void loadProgramFromFile(File file) {
        // Update UI state
        setLoadingState(true);
        updateSummary("Loading file...");
        if (loadStatusLabel != null) loadStatusLabel.setText("Starting...");
        if (loadProgress != null) loadProgress.setProgress(-1); // indeterminate

        Task<PresentProgramDTO> loadingTask = fileLoadingService.createLoadingTask(file);

        // Bind progress + message
        if (loadProgress != null) loadProgress.progressProperty().bind(loadingTask.progressProperty());
        if (loadStatusLabel != null) loadStatusLabel.textProperty().bind(loadingTask.messageProperty());

        loadingTask.setOnSucceeded(event -> {
            // Unbind first
            if (loadProgress != null) loadProgress.progressProperty().unbind();
            if (loadStatusLabel != null) loadStatusLabel.textProperty().unbind();

            PresentProgramDTO dto = loadingTask.getValue();
            try {
                updateUIWithLoadedDto(file, dto);
                try {
                    Engine engine = EngineImpl.getInstance();
                    loadedEngineProgram = engine.getLoadedProgram();
                } catch (NoProgramException e) {
                    showErrorDialog("Engine Warning", "Program loaded but engine did not expose SProgram: " + e.getMessage());
                }
                if (loadProgress != null) loadProgress.setProgress(1);
                if (loadStatusLabel != null) loadStatusLabel.setText("Loaded");
            } catch (Exception e) {
                showErrorDialog("Processing Error", "Error processing loaded program: " + e.getMessage());
                updateSummary("Error processing file: " + e.getMessage());
                if (loadProgress != null) loadProgress.setProgress(0);
                if (loadStatusLabel != null) loadStatusLabel.setText("Process error");
            } finally {
                setLoadingState(false);
            }
        });

        loadingTask.setOnFailed(event -> {
            if (loadProgress != null) loadProgress.progressProperty().unbind();
            if (loadStatusLabel != null) loadStatusLabel.textProperty().unbind();
            Throwable exception = loadingTask.getException();
            showErrorDialog("Loading Error", "Failed to load file: " + exception.getMessage());
            updateSummary("Failed to load file: " + exception.getMessage());
            if (loadProgress != null) loadProgress.setProgress(0);
            if (loadStatusLabel != null) loadStatusLabel.setText("Failed");
            setLoadingState(false);
        });

        Thread loadingThread = new Thread(loadingTask);
        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    /**
     * Updates UI with loaded program data from a PresentProgramDTO
     */
    private void updateUIWithLoadedDto(File file, PresentProgramDTO dto) {
        try {
            // Convert DTO to UI Program model
            Program uiProgram = ModelConverter.convertProgram(dto);

            // Update current program model
            currentProgram.setFilePath(file.getAbsolutePath());
            currentProgram.setName(uiProgram.getName());
            currentProgram.setLoaded(true);
            currentProgram.setMaxDegree(uiProgram.getMaxDegree());
            currentProgram.setMinDegree(uiProgram.getMinDegree());
            currentProgram.setTotalCycles(uiProgram.getTotalCycles());

            // Update UI collections using DTO-aware converters
            updateUICollections(dto);

            updateSummary("File loaded successfully: " + file.getName());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates UI collections with converted data (DTO path)
     */
    private void updateUICollections(PresentProgramDTO dto) {
        // Clear existing data
        instructions.clear();
        variables.clear();
        labels.clear();
        currentProgram.clearInstructions();
        currentProgram.clearVariables();

        // Convert and add instructions (DTO-based)
        List<Instruction> convertedInstructions = ModelConverter.convertInstructions(dto);
        instructions.addAll(convertedInstructions);
        for (Instruction instruction : convertedInstructions) {
            currentProgram.addInstruction(instruction);
        }

        // Convert and add variables (DTO-based)
        List<Variable> convertedVariables = ModelConverter.convertVariables(dto);
        variables.addAll(convertedVariables);
        for (Variable variable : convertedVariables) {
            currentProgram.addVariable(variable);
        }

        // Convert and add labels (DTO-based)
        List<SLabel> convertedLabels = ModelConverter.convertLabels(dto);
        labels.addAll(convertedLabels);

        // Update execution state
        executionResult.reset();
        executionResult.setTotalSteps(instructions.size());
    }

    private void handleStartRegular() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog("No Program", "Please load a program first.");
            return;
        }

        try {
            // Step 1: Get RunProgramDTO from engine
            Engine engine = EngineImpl.getInstance();
            RunProgramDTO runDTO = engine.runProgram();

            // Step 2: Handle degree selection
            if (runDTO.getMaxDegree() == 0) {
                updateSummary("Program cannot be expanded, will be executed as is.");
                runDTO.setDegree(0);
            } else {
                // For now, use degree 0 (original program). Later you can add UI for degree selection
                runDTO.setDegree(0);
                updateSummary("Executing with degree 0 (original program)");
            }

            // Step 3: Show input dialog for required inputs
            Set<core.logic.variable.Variable> requiredInputs = runDTO.getInputs();

            InputDialog inputDialog = new InputDialog(requiredInputs);
            inputDialog.initOwner(loadFileButton.getScene().getWindow());
            inputDialog.initModality(Modality.WINDOW_MODAL);

            Optional<List<Long>> inputResult = inputDialog.showAndWait();

            if (!inputResult.isPresent()) {
                updateSummary("Execution cancelled by user");
                return; // User cancelled
            }

            List<Long> inputValues = inputResult.get();
            runDTO.setInputs(inputValues);

            // Step 4: Execute the program
            updateSummary("Executing program with inputs: " + inputValues);
            executionResult.reset();
            executionResult.setRunning(true);
            executionResult.setStatus("Running");

            // Execute in background to keep UI responsive
            Task<Void> executionTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Run the program
                    core.logic.execution.ExecutionResult result = runDTO.runProgram();

                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        try {
                            updateUIAfterExecution(runDTO, result);
                        } catch (Exception e) {
                            showErrorDialog("Execution Error", "Error updating UI: " + e.getMessage());
                        }
                    });

                    return null;
                }
            };

            executionTask.setOnFailed(event -> {
                Throwable exception = executionTask.getException();
                showErrorDialog("Execution Error", "Execution failed: " + exception.getMessage());
                executionResult.setRunning(false);
                executionResult.setStatus("Failed");
                updateSummary("Execution failed: " + exception.getMessage());
            });

            Thread executionThread = new Thread(executionTask);
            executionThread.setDaemon(true);
            executionThread.start();

        } catch (Exception e) {
            showErrorDialog("Execution Error", "Failed to start execution: " + e.getMessage());
            updateSummary("Failed to start execution: " + e.getMessage());
        }
    }


    /**
     * Updates UI after program execution following ConsoleUI pattern
     */
    private void updateUIAfterExecution(RunProgramDTO runDTO, core.logic.execution.ExecutionResult result) throws Exception {
        // Update execution result
        executionResult.setCompleted(true);
        executionResult.setRunning(false);
        executionResult.setStatus("Completed");
        executionResult.setCycles(result.getCycles());

        // Get program representation (executed program, potentially expanded)
        PresentProgramDTO programPresent = runDTO.getPresentProgramDTO();
        executionResult.addToHistory("Program Representation updated");

        // Update instructions table with executed program
        instructions.clear();
        instructions.addAll(ModelConverter.convertInstructions(programPresent));

        // Get variables and their values (like ConsoleUI does)
        Set<core.logic.variable.Variable> programVariables = runDTO.getOrderedVariablesCopy();
        List<Long> variableValues = runDTO.getOrderedValuesCopy();

        // Update variables table with execution results
        updateVariablesTableWithResults(programVariables, variableValues);

        // Update summary with result
        updateSummary("Execution completed - Result: " + result.getResult() + ", Cycles: " + result.getCycles());

        // Add to execution history
        executionResult.addToHistory("Result: " + result.getResult());
        executionResult.addToHistory("Execution Cycles: " + result.getCycles());
    }

    /**
     * Updates variables table with execution results (like ConsoleUI variable display)
     */
    private void updateVariablesTableWithResults(Set<core.logic.variable.Variable> programVariables,
                                                 List<Long> variableValues) {
        variables.clear();

        if (programVariables == null || variableValues == null) {
            return;
        }

        // Convert to list to maintain order
        List<core.logic.variable.Variable> orderedVars = new ArrayList<>(programVariables);

        // Create UI variables with their execution values
        for (int i = 0; i < Math.min(orderedVars.size(), variableValues.size()); i++) {
            core.logic.variable.Variable engineVar = orderedVars.get(i);
            Long value = variableValues.get(i);

            // Create Variable for UI table with execution result
            Variable uiVar = new Variable(
                    engineVar.getRepresentation(),  // name
                    value.intValue(),               // value from execution
                    engineVar.getType().name()      // type
            );

            variables.add(uiVar);
        }

        executionResult.addToHistory("Variables updated with execution results");
    }

    /**
     * Shows dialog to select program execution degree
     */
    private int selectExecutionDegree(RunProgramDTO runDTO) {
        if (runDTO.getMaxDegree() == 0) {
            return 0; // No expansion possible
        }

        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Select Execution Degree");
        dialog.setHeaderText("Choose program expansion degree");
        dialog.setContentText("Enter degree (" + runDTO.getMinDegree() + " - " + runDTO.getMaxDegree() + "):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int degree = Integer.parseInt(result.get());
                if (degree >= runDTO.getMinDegree() && degree <= runDTO.getMaxDegree()) {
                    return degree;
                } else {
                    showErrorDialog("Invalid Degree", "Degree must be between " +
                            runDTO.getMinDegree() + " and " + runDTO.getMaxDegree());
                    return selectExecutionDegree(runDTO); // Recursive retry
                }
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Please enter a valid number.");
                return selectExecutionDegree(runDTO); // Recursive retry
            }
        }
        return 0; // Default if cancelled
    }


    private void handleStartDebug() {
        if (loadedEngineProgram == null) {
            showErrorDialog("No Program", "Please load a program first.");
            return;
        }

        // Start debug execution using service
        executionService.startDebugExecution(loadedEngineProgram);

        // Update UI state
        executionResult.reset();
        executionResult.setRunning(true);
        executionResult.setPaused(true);
        executionResult.setStatus("Debug Mode - Paused");
        executionResult.addToHistory("Debug execution started");
        updateSummary("Debug execution started - use step controls");
    }

    private void handleStepOver() {
        if (executionService.stepNext()) {
            int currentStep = executionResult.getCurrentStep();
            executionResult.setCurrentStep(currentStep + 1);

            if (currentStep < instructions.size()) {
                Instruction currentInstr = instructions.get(currentStep);
                executionResult.setCurrentInstruction(currentInstr.getDescription());
                executionResult.addToHistory("Step " + (currentStep + 1) + ": " + currentInstr.getDescription());

                if (currentStep + 1 >= instructions.size()) {
                    executionResult.setCompleted(true);
                    executionResult.setRunning(false);
                    executionResult.setStatus("Completed");
                }
            }

            updateSummary("Stepped over instruction");
        }
    }

    private void handleStepBack() {
        if (executionService.stepBack()) {
            int currentStep = executionResult.getCurrentStep();
            if (currentStep > 0) {
                executionResult.setCurrentStep(currentStep - 1);
                executionResult.addToHistory("Stepped back to instruction " + currentStep);
                updateSummary("Stepped back instruction");
            }
        }
    }

    private void handleStop() {
        executionService.stopExecution();
        executionResult.setRunning(false);
        executionResult.setPaused(false);
        executionResult.setStatus("Stopped");
        updateSummary("Execution stopped");
    }

    private void handleResume() {
        executionService.resumeExecution();
        executionResult.setPaused(false);
        executionResult.setStatus("Running");
        updateSummary("Execution resumed");
    }

    private void handleCollapse() {
        // TODO: Implement program collapse logic using expansion service
        updateSummary("Program collapsed");
    }

    private void handleExpand() {
        // TODO: Implement program expansion logic using expansion service
        updateSummary("Program expanded");
    }

    private void handleHighlight() {
        // TODO: Implement instruction highlighting
        updateSummary("Instructions highlighted");
    }

    private void handleShowStats() {
        // TODO: Show detailed statistics using statistics service
        updateSummary("Statistics displayed");
    }

    private void handleRerun() {
        if (loadedEngineProgram != null) {
            handleStartRegular(); // Rerun the last loaded program
            updateSummary("Rerunning last execution");
        } else {
            showErrorDialog("No Program", "Please load a program first.");
        }
    }

    // **UI UTILITY METHODS**

    /**
     * Sets loading state for UI components
     */
    private void setLoadingState(boolean loading) {
        loadFileButton.setDisable(loading);
        // Could disable other buttons during loading if needed
    }

    /**
     * Updates UI with execution results using actual ExecutionResult data
     */
    private void updateUIWithExecutionResult(core.logic.execution.ExecutionResult result) {
        // Update UI execution result with actual data from engine
        executionResult.setCompleted(true);
        executionResult.setRunning(false);
        executionResult.setStatus("Completed");
        executionResult.setCycles(result.getCycles());
        executionResult.addToHistory("Execution completed with result: " + result.getResult());

        // Update the summary with actual result
        updateSummary("Execution completed - Result: " + result.getResult() + ", Cycles: " + result.getCycles());
    }

    /**
     * Shows error dialog (UI logic only)
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Updates summary line (UI logic only)
     */
    private void updateSummary(String message) {
        summaryLine.setText(message);
    }
}

