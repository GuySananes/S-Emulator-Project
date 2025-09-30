package javafxUI.controller;

import core.logic.engine.Engine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafxUI.model.ui.*;
import javafxUI.service.ModelConverter;
import javafxUI.service.ThemeManager;
import present.mostInstructions.PresentInstructionDTO;
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

    // Theme toggle button
    @FXML private Button themeToggleButton;

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

    // Theme manager
    private ThemeManager themeManager;

    @FXML
    public void initialize() {
        initializeThemeManager();
        initializeSubControllers();
        setupTables();
        setupEventHandlers();
        setupDataBinding();
    }

    private void initializeThemeManager() {
        themeManager = ThemeManager.getInstance();
        updateThemeButtonText();
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

        // Set the callback for instruction expansion (historical chain display)
        tableController.setInstructionExpansionCallback(this::showInstructionHistory);
    }


    // Modify this method to expand instruction into the table instead of a history chain
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


    private void setupEventHandlers() {
        fileLoadingController.setupEventHandlers();
        executionController.setupEventHandlers();

        // Theme toggle button
        if (themeToggleButton != null) {
            themeToggleButton.setOnAction(e -> handleThemeToggle());
        }

        // Program selector (stays here as it's simple)
        programSelector.setOnAction(e -> handleProgramSelection());

        // Program controls (delegate to execution controller)
        collapseButton.setOnAction(e -> executionController.handleCollapse());
        expandButton.setOnAction(e -> executionController.handleExpand());

        // Clear highlight button functionality
        highlightButton.setOnAction(e -> handleClearHighlightClick());

        // Add Apply button handler (this was missing!)
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

    private void handleThemeToggle() {
        themeManager.toggleTheme();
        updateThemeButtonText();
        updateSummary("Theme switched to " + (themeManager.isDarkMode() ? "Dark" : "Light") + " mode");
    }

    private void updateThemeButtonText() {
        if (themeToggleButton != null) {
            if (themeManager.isDarkMode()) {
                themeToggleButton.setText("🌙 Dark Mode");
            } else {
                themeToggleButton.setText("☀️ Light Mode");
            }
        }
    }

    // Public method for Main.java to set the scene
    public void setScene(javafx.scene.Scene scene) {
        themeManager.setScene(scene);
        // Apply initial theme
        themeManager.setTheme(ThemeManager.Theme.DARK);
    }

    private void handleClearHighlightClick() {
        // Clear the input field
        if (highlightInputField != null) {
            highlightInputField.clear();
        }
        // Clear the highlighting
        tableController.clearHighlighting();
        updateSummary("Highlighting cleared");
    }

    // New method to handle apply button click
    private void handleApplyHighlightClick() {
        if (highlightInputField != null) {
            String inputText = highlightInputField.getText().trim();
            if (inputText.isEmpty()) {
                // Clear highlighting if input is empty
                tableController.clearHighlighting();
                updateSummary("Highlighting cleared - input field is empty");
            } else {
                // Highlight the variable/label
                tableController.highlightVariable(inputText);
                updateSummary("Highlighting instructions containing: " + inputText);
            }
        }
    }

    // Keep the real-time highlighting method
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

    // Add this method to get the instruction chain
    private String getInstructionChain(Instruction instruction) {
        StringBuilder chain = new StringBuilder();
        chain.append("Instruction Chain for #").append(instruction.getNumber()).append(":\n");
        chain.append("Type: ").append(instruction.getType()).append("\n");
        chain.append("Cycles: ").append(instruction.getCycles()).append("\n");
        chain.append("Description: ").append(instruction.getDescription()).append("\n");
        chain.append("---\n");

        try {
            // Get the historical chain using the DTO parents
            List<Instruction> historicalChain = buildHistoricalChain(instruction);

            if (!historicalChain.isEmpty()) {
                chain.append("\nHistorical Chain (from parent to current):\n");
                for (int i = 0; i < historicalChain.size(); i++) {
                    Instruction chainInst = historicalChain.get(i);
                    chain.append(String.format("%d. #%d [%s] - %s (%d cycles)\n",
                            i + 1, chainInst.getNumber(), chainInst.getType(),
                            chainInst.getDescription(), chainInst.getCycles()));
                }
            } else {
                chain.append("\nNo parent instructions found - this is a root instruction.\n");
            }

        } catch (Exception e) {
            chain.append("\nError retrieving historical chain: ").append(e.getMessage()).append("\n");
        }

        return chain.toString();
    }

    /**
     * Builds the historical chain for an instruction using the DTO parents
     */
    private List<Instruction> buildHistoricalChain(Instruction targetInstruction) throws Exception {
        List<Instruction> historicalChain = new ArrayList<>();

        System.out.println("=== DEBUG: buildHistoricalChain for instruction #" + targetInstruction.getNumber() + " ===");

        try {
            Engine engine = Engine.getInstance();
            System.out.println("Engine instance obtained: " + (engine != null));

            PresentProgramDTO currentProgram = engine.presentProgram();
            System.out.println("Current program obtained: " + (currentProgram != null));

            if (currentProgram == null) {
                throw new Exception("No program is currently loaded");
            }

            System.out.println("Program name: " + currentProgram.getProgramName());
            System.out.println("Number of instructions in DTO: " +
                    (currentProgram.getInstructionList() != null ? currentProgram.getInstructionList().size() : "null"));

            // Find the corresponding PresentInstructionDTO for this instruction
            PresentInstructionDTO targetDTO = findInstructionDTO(currentProgram, targetInstruction);
            System.out.println("Found matching DTO: " + (targetDTO != null));

            if (targetDTO != null) {
                System.out.println("DTO representation: " + targetDTO.getRepresentation());
                System.out.println("DTO parents count: " +
                        (targetDTO.getParents() != null ? targetDTO.getParents().size() : "null"));

                // Build the chain from parents using recursive traversal
                buildChainFromParents(targetDTO, historicalChain);
                System.out.println("Historical chain built with " + historicalChain.size() + " parents");
            } else {
                System.out.println("Could not find matching DTO - instruction might be generated/expanded");
            }

        } catch (Exception e) {
            System.err.println("=== ERROR in buildHistoricalChain ===");
            System.err.println("Error for instruction #" + targetInstruction.getNumber() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return historicalChain;
    }

    /**
     * Recursively builds the chain from DTO parents
     */
    private void buildChainFromParents(PresentInstructionDTO instructionDTO, List<Instruction> chain) {
        System.out.println("=== DEBUG: buildChainFromParents ===");
        System.out.println("Processing DTO: " + instructionDTO.getRepresentation());

        List<PresentInstructionDTO> parents = instructionDTO.getParents();
        System.out.println("Parents: " + (parents != null ? parents.size() : "null"));

        if (parents != null && !parents.isEmpty()) {
            System.out.println("Processing " + parents.size() + " parent(s):");

            // Process parents first (to get them at the beginning of the chain)
            for (int i = 0; i < parents.size(); i++) {
                PresentInstructionDTO parent = parents.get(i);
                System.out.println("Parent[" + i + "]: " + parent.getRepresentation());

                buildChainFromParents(parent, chain); // Recursive call for grandparents

                Instruction convertedParent = convertDTOToInstruction(parent);
                chain.add(convertedParent);
                System.out.println("Added parent to chain: #" + convertedParent.getNumber() + " - " + convertedParent.getDescription());
            }
        } else {
            System.out.println("No parents to process - reached root instruction");
        }

        System.out.println("Chain now has " + chain.size() + " instructions");
    }


    /**
     * Finds the PresentInstructionDTO that corresponds to the UI Instruction
     */
    private PresentInstructionDTO findInstructionDTO(PresentProgramDTO programDTO, Instruction targetInstruction) {
        System.out.println("=== DEBUG: findInstructionDTO ===");
        System.out.println("Looking for instruction #" + targetInstruction.getNumber() + ": " + targetInstruction.getDescription());

        if (programDTO == null) {
            System.err.println("Program DTO is null");
            return null;
        }

        List<PresentInstructionDTO> instructionList = programDTO.getInstructionList();
        if (instructionList == null) {
            System.err.println("Instruction list is null");
            return null;
        }

        System.out.println("Searching through " + instructionList.size() + " DTOs:");

        // Find by matching instruction number/index and description
        for (int i = 0; i < instructionList.size(); i++) {
            PresentInstructionDTO dto = instructionList.get(i);
            if (dto != null) {
                System.out.println("DTO[" + i + "]: index=" + dto.getIndex() +
                        ", representation=" + dto.getRepresentation());

                if (matchesInstruction(dto, targetInstruction)) {
                    System.out.println("*** MATCH FOUND at index " + i + " ***");
                    return dto;
                }
            }
        }

        System.out.println("No matching DTO found for instruction #" + targetInstruction.getNumber());
        return null;
    }

    /**
     * Checks if a DTO matches the target instruction
     */
    private boolean matchesInstruction(PresentInstructionDTO dto, Instruction targetInstruction) {
        // Debug print
        System.out.println("  Comparing DTO(index=" + dto.getIndex() + ") with Instruction(number=" + targetInstruction.getNumber() + ")");

        // Match by index (instruction number) - this is the primary match
        if (dto.getIndex() == targetInstruction.getNumber()) {
            System.out.println("  → Index match!");
            return true;
        }

        // Also try to match by representation similarity as secondary
        String dtoDesc = dto.getRepresentation();
        String targetDesc = targetInstruction.getDescription();

        if (dtoDesc != null && targetDesc != null) {
            // Remove expansion markers and compare core content
            String cleanDtoDesc = dtoDesc.replaceAll("^>+\\s*", "").trim();
            String cleanTargetDesc = targetDesc.replaceAll("^>+\\s*", "").trim();

            if (cleanDtoDesc.equals(cleanTargetDesc)) {
                System.out.println("  → Description match!");
                return true;
            }
        }

        return false;
    }


    /**
     * Converts a PresentInstructionDTO to a UI Instruction object
     */
    private Instruction convertDTOToInstruction(PresentInstructionDTO dto) {
        String representation = dto.getRepresentation();
        if (representation == null) {
            representation = "Unknown instruction";
        }

        // Clean up the representation - remove the prefix like "#1 (S) [  ]"
        String cleanDescription = representation;
        if (representation.matches("^#\\d+\\s+\\([BS]\\)\\s+.*")) {
            // Extract just the instruction part after the type marker
            cleanDescription = representation.replaceFirst("^#\\d+\\s+\\([BS]\\)\\s+", "");
        }

        // Extract type from the DTO
        String type = deriveTypeFromDTO(dto);

        // Get cycles from the DTO
        int cycles = extractCyclesFromDTO(dto);

        return new Instruction(
                dto.getIndex(),
                type,
                cycles,
                cleanDescription
        );
    }

    /**
     * Updates the historical chain table with the chain for the given instruction
     */
    private void updateHistoricalChainTable(Instruction instruction) {
        System.out.println("=== DEBUG: updateHistoricalChainTable ===");

        try {
            List<Instruction> chain = buildHistoricalChain(instruction);
            System.out.println("Built chain with " + chain.size() + " instructions");

            // Create the ordered chain: selected instruction first, then parents in order
            List<Instruction> orderedChain = new ArrayList<>();

            // Add the selected instruction at the top
            orderedChain.add(instruction);
            System.out.println("Added selected instruction: #" + instruction.getNumber());

            // Add the parent chain below it
            orderedChain.addAll(chain);
            System.out.println("Final ordered chain has " + orderedChain.size() + " instructions:");
            for (int i = 0; i < orderedChain.size(); i++) {
                Instruction inst = orderedChain.get(i);
                System.out.println("  [" + i + "] #" + inst.getNumber() + " (" + inst.getType() + ") - " + inst.getDescription());
            }

            // Update the instruction's historical chain
            instruction.setHistoricalChain(FXCollections.observableArrayList(orderedChain));
            System.out.println("Set historical chain on instruction object");

            // Update the controller's historical chain table
            if (tableController != null) {
                System.out.println("Calling tableController.updateHistoricalChainTable...");
                tableController.updateHistoricalChainTable(orderedChain);
                System.out.println("tableController.updateHistoricalChainTable called");
            } else {
                System.err.println("ERROR: tableController is null!");
            }

            // Skip the TextArea update since it's null and not needed
            System.out.println("Skipped TextArea update - using table display instead");

        } catch (Exception e) {
            System.err.println("=== ERROR in updateHistoricalChainTable ===");
            e.printStackTrace();
            // Show more detailed error information
            String errorMsg = "Failed to build historical chain: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += "\nCause: " + e.getCause().getMessage();
            }
            showErrorDialog("Chain Error", errorMsg);
        }

        System.out.println("=== END updateHistoricalChainTable ===");
    }

    /**
     * Public method to get historical chain for an instruction (can be called from TableController)
     */
    public void showInstructionHistory(Instruction instruction) {
        System.out.println("=== DEBUG: showInstructionHistory called for instruction #" + instruction.getNumber() + " ===");
        System.out.println("Instruction description: " + instruction.getDescription());
        System.out.println("Instruction type: " + instruction.getType());

        try {
            updateHistoricalChainTable(instruction);
            updateSummary("Showing historical chain for instruction #" + instruction.getNumber());
        } catch (Exception e) {
            System.err.println("=== ERROR in showInstructionHistory ===");
            e.printStackTrace();
            showErrorDialog("Chain Error", "Failed to show instruction history: " + e.getMessage());
        }
    }


    /**
     * Derives instruction type from DTO
     */
    private String deriveTypeFromDTO(PresentInstructionDTO dto) {
        // First try to get the type from InstructionData if available
        if (dto.getInstructionData() != null) {
            return dto.getInstructionData().getInstructionType(); // This returns "B" or "S"
        }

        // Fallback to parsing the representation
        String representation = dto.getRepresentation();
        if (representation == null) {
            return "B"; // Default to Basic
        }

        // Simple fallback logic based on representation
        String cleanRep = representation.replaceAll("^>+\\s*", "").trim();

        // Basic instructions are typically single operations
        if (cleanRep.matches(".*\\+\\+.*") || cleanRep.matches(".*--.*") ||
                cleanRep.startsWith("JNZ") || cleanRep.startsWith("INC") ||
                cleanRep.startsWith("DEC") || cleanRep.startsWith("NOOP")) {
            return "B";
        }

        // Everything else is likely S (Super)
        return "S";
    }

    /**
     * Extracts cycles information from DTO
     */
    private int extractCyclesFromDTO(PresentInstructionDTO dto) {
        // Try to get cycles from InstructionData first
        if (dto.getInstructionData() != null) {
            String cycleRep = dto.getInstructionData().getCycleRepresentation();
            if (cycleRep != null) {
                try {
                    // Handle simple numeric cycles
                    if (cycleRep.matches("\\d+")) {
                        return Integer.parseInt(cycleRep);
                    }
                    // For complex expressions like "execution + 5", return a default
                    if (cycleRep.contains("execution")) {
                        return 5; // Default for complex cycles
                    }
                } catch (NumberFormatException e) {
                    // Fall through to default
                }
            }
        }

        // Default cycles
        return 1;
    }
}