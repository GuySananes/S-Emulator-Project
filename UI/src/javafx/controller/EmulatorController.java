
package javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.model.ui.*;

/**
 * Main controller that coordinates all sub-controllers
 */
public class EmulatorController {

    // FXML UI Components (only the main ones)
    @FXML private Button loadFileButton;
    @FXML private TextField loadedFilePath;
    @FXML private ComboBox<String> programSelector;
    @FXML private TableView<Instruction> instructionsTable;
    @FXML private TableView<Variable> variablesTable;
    @FXML private TableView<Statistic> statisticsTable;
    @FXML private Label summaryLine;
    @FXML private TextArea historyChain;
    @FXML private Label cyclesLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private Label loadStatusLabel;

    // All execution/debug buttons
    @FXML private Button startRegularButton;
    @FXML private Button startDebugButton;
    @FXML private Button stopButton;
    @FXML private Button resumeButton;
    @FXML private Button stepOverButton;
    @FXML private Button stepBackButton;

    // Program control buttons
    @FXML private Button collapseButton;
    @FXML private Button expandButton;
    @FXML private Button highlightButton;
    @FXML private Button showStatsButton;
    @FXML private Button rerunButton;

    // Model objects (shared with sub-controllers)
    private final Program currentProgram = new Program();
    private final ExecutionResult executionResult = new ExecutionResult();
    private final ObservableList<Instruction> instructions = FXCollections.observableArrayList();
    private final ObservableList<Variable> variables = FXCollections.observableArrayList();
    private final ObservableList<Statistic> statistics = FXCollections.observableArrayList();

    // Sub-controllers
    private FileLoadingController fileLoadingController;
    private ProgramExecutionController executionController;
    private TableController tableController;
    private UIBindingController bindingController;

    @FXML
    public void initialize() {
        initializeSubControllers();
        setupTables();
        setupEventHandlers();
        setupDataBinding();
    }

    private void initializeSubControllers() {
        // Pass shared dependencies to sub-controllers
        fileLoadingController = new FileLoadingController(
                currentProgram, instructions, variables,
                loadFileButton, loadedFilePath, loadProgress, loadStatusLabel,
                this::updateSummary, this::showErrorDialog
        );

        executionController = new ProgramExecutionController(
                currentProgram, executionResult, instructions, variables,
                startRegularButton, startDebugButton, stopButton, resumeButton,
                stepOverButton, stepBackButton, rerunButton,
                this::updateSummary, this::showErrorDialog
        );

        tableController = new TableController(
                instructionsTable, variablesTable, statisticsTable,
                instructions, variables, statistics
        );

        bindingController = new UIBindingController(
                currentProgram, executionResult,
                loadedFilePath, cyclesLabel, historyChain
        );
    }

    private void setupTables() {
        tableController.setupAllTables();
    }

    private void setupEventHandlers() {
        fileLoadingController.setupEventHandlers();
        executionController.setupEventHandlers();

        // Program selector (stays here as it's simple)
        programSelector.setOnAction(e -> handleProgramSelection());

        // Program controls (delegate to execution controller)
        collapseButton.setOnAction(e -> executionController.handleCollapse());
        expandButton.setOnAction(e -> executionController.handleExpand());
        highlightButton.setOnAction(e -> executionController.handleHighlight());
        showStatsButton.setOnAction(e -> executionController.handleShowStats());
    }

    private void setupDataBinding() {
        bindingController.setupAllBindings();
    }

    private void handleProgramSelection() {
        String selectedProgram = programSelector.getValue();
        if (selectedProgram != null) {
            currentProgram.setName(selectedProgram);
            updateSummary("Switched to program: " + selectedProgram);
        }
    }

    // Utility methods used by sub-controllers
    public void updateSummary(String message) {
        summaryLine.setText(message);
    }

    public void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}