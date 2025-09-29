
package javafxUI.controller;

import core.logic.engine.Engine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafxUI.model.ui.*;
import javafx.scene.control.*;
import javafxUI.model.ui.*;
import javafxUI.service.ModelConverter;
import present.program.PresentProgramDTO;

import java.util.ArrayList;
import java.util.List;


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
    @FXML private Label currentDegreeLabel;

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
    @FXML private TableView<Instruction> historicalChainTable = new TableView<>();
    @FXML private TextField highlightInputField;
    @FXML private Button applyHighlightButton;

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
                currentProgram, executionResult, instructions, variables, statistics, // Add statistics here
                startRegularButton, startDebugButton, stopButton, resumeButton,
                stepOverButton, stepBackButton, rerunButton,
                this::updateSummary, this::showErrorDialog
        );

        tableController = new TableController(
                instructionsTable, variablesTable, statisticsTable,
                historicalChainTable,instructions, variables, statistics
        );

        bindingController = new UIBindingController(
                currentProgram, executionResult,
                loadedFilePath, cyclesLabel, historyChain,
                currentDegreeLabel
        );
    }

    private void setupTables() {
        tableController.setupAllTables();

        // Set up the instruction expansion callback
        tableController.setInstructionExpansionCallback(this::expandInstruction);
    }

        // Modify this method to expand instruction into the table instead of history chain
    private void expandInstruction(Instruction instruction) {
        try {
            // Get expanded instructions for this specific instruction
            List<Instruction> expandedInstructions = getExpandedInstructionsForInstruction(instruction);

            // Replace the instructions table content with only the expanded instructions
            instructions.clear();
            instructions.addAll(expandedInstructions);

            // Update the history chain to show what instruction was expanded
            historyChain.setText("Expanded view of instruction #" + instruction.getNumber() +
                    ": " + instruction.getDescription() +
                    "\n\nShowing " + expandedInstructions.size() + " decomposed instruction(s)");

            // Update summary
            updateSummary("Showing expanded view of instruction #" + instruction.getNumber());

        } catch (Exception e) {
            showErrorDialog("Expansion Error", "Failed to expand instruction: " + e.getMessage());
        }
    }

    // Add this new method to get expanded instructions for a specific instruction
    // Add this new method to get expanded instructions for a specific instruction
    private List<Instruction> getExpandedInstructionsForInstruction(Instruction targetInstruction) throws Exception {
        Engine engine = Engine.getInstance(); // Changed from EngineImpl.getInstance()

        // Use the engine's expandOrShrinkProgram method to get expanded view
        PresentProgramDTO expandedProgram = engine.expandOrShrinkProgram(1); // Start with degree 1

        // Convert all expanded instructions
        List<Instruction> allExpandedInstructions = ModelConverter.convertInstructions(expandedProgram);

        // Filter to find only the instructions that correspond to the selected instruction
        List<Instruction> relevantInstructions = new ArrayList<>();

        // This is a simplified approach - you might need to adjust based on your DTO structure
        // Look for instructions that are related to the original instruction number
        for (Instruction expandedInst : allExpandedInstructions) {
            // You may need to implement logic here to identify which expanded instructions
            // correspond to the original instruction. This could be based on:
            // - Line number ranges
            // - Special markers in the description
            // - DTO metadata

            if (isRelatedToOriginalInstruction(expandedInst, targetInstruction)) {
                relevantInstructions.add(expandedInst);
            }
        }

        // If no specific mapping is found, return a clean version of the original instruction
        if (relevantInstructions.isEmpty()) {
            relevantInstructions.add(createCleanInstruction(targetInstruction));
        }

        return relevantInstructions;
    }

    // Helper method to determine if an expanded instruction relates to the original
    private boolean isRelatedToOriginalInstruction(Instruction expandedInst, Instruction originalInst) {
        // This logic depends on how your expansion system marks related instructions
        // You might need to check:
        // - If the description contains references to the original instruction
        // - If there are special markers or patterns
        // - If the line numbers fall within a certain range

        String expandedDesc = expandedInst.getDescription().toLowerCase();
        String originalDesc = originalInst.getDescription().toLowerCase();

        // Remove ">>>> " prefixes and other expansion markers
        expandedDesc = expandedDesc.replaceAll("^>+\\s*", "");

        // Simple heuristic: check if descriptions are similar or if there's a reference
        return expandedDesc.contains(originalDesc.replaceAll("\\[.*?\\]", "").trim()) ||
                expandedInst.getNumber() == originalInst.getNumber();
    }

    // Helper method to create a clean version of an instruction without expansion markers
    private Instruction createCleanInstruction(Instruction original) {
        String cleanDescription = original.getDescription();
        // Remove any expansion markers like ">>>>"
        cleanDescription = cleanDescription.replaceAll("^>+\\s*", "");

        return new Instruction(
                original.getNumber(),
                original.getType(),
                original.getCycles(),
                cleanDescription
        );
    }

    // Add this method to get the instruction chain
    private String getInstructionChain(Instruction instruction) {
        // This is where you'll implement the logic to get the instruction chain
        // You might need to call your DTO services or expansion logic here

        StringBuilder chain = new StringBuilder();
        chain.append("Instruction Chain for #").append(instruction.getNumber()).append(":\n");
        chain.append("Type: ").append(instruction.getType()).append("\n");
        chain.append("Cycles: ").append(instruction.getCycles()).append("\n");
        chain.append("Description: ").append(instruction.getDescription()).append("\n");
        chain.append("---\n");

        // TODO: Add actual expansion logic here
        // You may need to use your ExpandDTO or other services to get the real chain
        // For example:
        // if (currentProgram.hasExpandableInstructions()) {
        //     ExpandDTO expandDTO = // get expand DTO
        //     String expandedChain = expandDTO.expandInstruction(instruction.getNumber());
        //     chain.append(expandedChain);
        // }

        return chain.toString();
    }



    private void setupEventHandlers() {
        fileLoadingController.setupEventHandlers();
        executionController.setupEventHandlers();

        // Program selector (stays here as it's simple)
        programSelector.setOnAction(e -> handleProgramSelection());

        // Program controls (delegate to execution controller)
        collapseButton.setOnAction(e -> executionController.handleCollapse());
        expandButton.setOnAction(e -> executionController.handleExpand());

        // Modified highlight button to use the input field
        highlightButton.setOnAction(e -> handleHighlightButtonClick());

        // Add Apply button handler
        if (applyHighlightButton != null) {
            applyHighlightButton.setOnAction(e -> handleApplyHighlightClick());
        }

        showStatsButton.setOnAction(e -> executionController.handleShowStats());

        // Add real-time highlighting as user types
        if (highlightInputField != null) {
            highlightInputField.textProperty().addListener((observable, oldValue, newValue) -> {
                handleHighlightInputChange(newValue);
            });
        }
    }

    // New method to handle apply button click
    private void handleApplyHighlightClick() {
        if (highlightInputField != null) {
            String inputText = highlightInputField.getText().trim();
            if (inputText.isEmpty()) {
                // Clear highlighting if input is empty
                tableController.clearHighlighting();
                updateSummary("Highlighting cleared");
            } else {
                // Highlight the variable/label
                tableController.highlightVariable(inputText);
                updateSummary("Highlighting instructions containing: " + inputText);
            }
        }
    }

    // New method to handle highlight button click
    private void handleHighlightButtonClick() {
        if (highlightInputField != null) {
            String inputText = highlightInputField.getText().trim();
            if (inputText.isEmpty()) {
                // Clear highlighting if input is empty
                tableController.clearHighlighting();
                updateSummary("Highlighting cleared");
            } else {
                // Highlight the variable/label
                tableController.highlightVariable(inputText);
                updateSummary("Highlighting instructions containing: " + inputText);
            }
        }
    }

    // New method to handle real-time highlighting as user types
    private void handleHighlightInputChange(String newValue) {
        if (newValue == null || newValue.trim().isEmpty()) {
            // Clear highlighting if input is empty
            tableController.clearHighlighting();
        } else {
            // Highlight the variable/label in real-time
            tableController.highlightVariable(newValue.trim());
        }
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