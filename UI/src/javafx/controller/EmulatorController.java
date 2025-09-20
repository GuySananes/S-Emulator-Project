package javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// Explicitly import JavaFX Label to avoid confusion with SLabel
import javafx.scene.control.Label;

public class EmulatorController {

    // File section
    @FXML private Button loadFileButton;
    @FXML private TextField loadedFilePath;

    // Program controls
    @FXML private ComboBox<String> programSelector;
    @FXML private Button collapseButton;
    @FXML private Label currentDegreeLabel;  // This is JavaFX Label control
    @FXML private Button expandButton;
    @FXML private Button highlightButton;

    // Instructions table
    @FXML private TableView<Instruction> instructionsTable;

    // Summary
    @FXML private Label summaryLine;  // This is JavaFX Label control

    // History chain
    @FXML private TextArea historyChain;

    // Debugger section
    @FXML private Button startRegularButton;
    @FXML private Button startDebugButton;
    @FXML private Button stopButton;
    @FXML private Button resumeButton;
    @FXML private Button stepOverButton;
    @FXML private Button stepBackButton;

    // Variables and execution
    @FXML private TableView<Variable> variablesTable;
    @FXML private TextArea executionInputs;
    @FXML private Label cyclesLabel;  // This is JavaFX Label control

    // Statistics
    @FXML private TableView<Statistic> statisticsTable;
    @FXML private Button showStatsButton;
    @FXML private Button rerunButton;

    // Model objects
    private final Program currentProgram = new Program();
    private final ExecutionResult executionResult = new ExecutionResult();
    private final ObservableList<Instruction> instructions = FXCollections.observableArrayList();
    private final ObservableList<Variable> variables = FXCollections.observableArrayList();
    private final ObservableList<Statistic> statistics = FXCollections.observableArrayList();
    private final ObservableList<SLabel> labels = FXCollections.observableArrayList();  // This is your SLabel model

    // This method is automatically called after the fxml file has been loaded
    @FXML
    public void initialize() {
        setupTables();
        setupEventHandlers();
        setupDataBinding();

        // Add some sample data for testing
        addSampleData();
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

    private void addSampleData() {
        // Sample program setup
        currentProgram.setName("Sample Program");
        currentProgram.setMinDegree(1);
        currentProgram.setMaxDegree(3);

        // Sample instructions
        instructions.addAll(
            new Instruction(1, "S", 1, "inc(x)"),
            new Instruction(2, "B", 2, "dec(y)"),
            new Instruction(3, "S", 1, "zero(z)")
        );

        // Add instructions to program
        for (Instruction instruction : instructions) {
            currentProgram.addInstruction(instruction);
        }

        // Sample variables
        variables.addAll(
            new Variable("x", 5, "input"),
            new Variable("y", 3, "temp"),
            new Variable("z", 0, "output")
        );

        // Add variables to program
        for (Variable variable : variables) {
            currentProgram.addVariable(variable);
        }

        // Sample statistics
        statistics.add(new Statistic("Regular", 15, 10, "00:00:05"));

        // Sample labels - now using SLabel instead of Label
        labels.addAll(
            new SLabel("START", 1, "entry"),
            new SLabel("LOOP", 5, "jump"),
            new SLabel("END", 10, "exit")
        );

        // Populate program selector
        programSelector.getItems().addAll("Main Program", "Function A", "Function B");
        programSelector.setValue("Main Program");

        // Set initial execution state
        executionResult.setStatus("Ready");
        executionResult.setTotalSteps(instructions.size());
    }

    // Event handler methods
    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open S-Program File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        Stage stage = (Stage) loadFileButton.getScene().getWindow();
        java.io.File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // Update program model
            currentProgram.setFilePath(file.getAbsolutePath());
            currentProgram.setName(file.getName().replace(".xml", ""));
            currentProgram.setLoaded(true);

            // TODO: Connect to your engine's file loading logic
            // Engine engine = new EngineImpl();
            // SProgram loadedProgram = engine.loadProgram(file);
            // convertEngineToUIModel(loadedProgram);

            updateSummary("File loaded: " + file.getName());
        }
    }

    private void handleCollapse() {
        // TODO: Implement program collapse logic
        updateSummary("Program collapsed");
    }

    private void handleExpand() {
        // TODO: Implement program expansion logic
        updateSummary("Program expanded");
    }

    private void handleHighlight() {
        // TODO: Implement instruction highlighting
        updateSummary("Instructions highlighted");
    }

    private void handleStartRegular() {
        // Reset execution state
        executionResult.reset();
        executionResult.setRunning(true);
        executionResult.setStatus("Running");
        executionResult.addToHistory("Execution started");

        // TODO: Connect to your engine's execution logic
        // ProgramExecutor executor = new ProgramExecutorImpl();
        // ExecutionResult result = executor.executeProgram(currentProgram);

        updateSummary("Regular execution started");
    }

    private void handleStartDebug() {
        executionResult.reset();
        executionResult.setRunning(true);
        executionResult.setPaused(true);
        executionResult.setStatus("Debug Mode - Paused");
        executionResult.addToHistory("Debug execution started");

        // TODO: Connect to your engine's debug execution logic

        updateSummary("Debug execution started");
    }

    private void handleStop() {
        // TODO: Stop execution
        updateSummary("Execution stopped");
        cyclesLabel.setText("Stopped");
    }

    private void handleResume() {
        // TODO: Resume execution
        updateSummary("Execution resumed");
    }

    private void handleStepOver() {
        if (executionResult.isRunning() && executionResult.isPaused()) {
            int currentStep = executionResult.getCurrentStep();
            executionResult.setCurrentStep(currentStep + 1);

            // Simulate stepping through instruction
            if (currentStep < instructions.size()) {
                Instruction currentInstr = instructions.get(currentStep);
                executionResult.setCurrentInstruction(currentInstr.getDescription());
                executionResult.addToHistory("Step " + (currentStep + 1) + ": " + currentInstr.getDescription());

                // Check if execution is complete
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
        // TODO: Step back instruction
        updateSummary("Stepped back instruction");
    }

    private void handleShowStats() {
        // TODO: Show detailed statistics
        updateSummary("Statistics displayed");
    }

    private void handleRerun() {
        // TODO: Rerun last execution
        updateSummary("Rerunning last execution");
    }

    private void updateSummary(String message) {
        summaryLine.setText(message);
    }
}
