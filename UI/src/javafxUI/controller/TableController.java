
package javafxUI.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafxUI.model.ui.Instruction;
import javafxUI.model.ui.Statistic;
import javafxUI.model.ui.Variable;

import java.util.List;

/**
 * Handles all table setup and configuration
 */
public class TableController {

    private final TableView<Instruction> instructionsTable;
    private final TableView<Variable> variablesTable;
    private final TableView<Statistic> statisticsTable;

    // New table for historical chain
    private final TableView<Instruction> historicalChainTable;
    private final ObservableList<Instruction> historicalChainData = FXCollections.observableArrayList();

    private final ObservableList<Instruction> instructions;
    private final ObservableList<Variable> variables;
    private final ObservableList<Statistic> statistics;

    private java.util.function.Consumer<Instruction> instructionExpansionCallback;

    // track current highlighted variable
    private String currentHighlightedVariable = null;

    public TableController(TableView<Instruction> instructionsTable,
                           TableView<Variable> variablesTable,
                           TableView<Statistic> statisticsTable,
                           TableView<Instruction> historicalChainTable,
                           ObservableList<Instruction> instructions,
                           ObservableList<Variable> variables,
                           ObservableList<Statistic> statistics) {
        this.instructionsTable = instructionsTable;
        this.variablesTable = variablesTable;
        this.statisticsTable = statisticsTable;
        this.historicalChainTable = historicalChainTable;
        this.instructions = instructions;
        this.variables = variables;
        this.statistics = statistics;
    }

    public void setupAllTables() {
        setupInstructionsTable();
        setupVariablesTable();
        setupStatisticsTable();
        setupHistoricalChainTable();
    }

    public void setInstructionExpansionCallback(java.util.function.Consumer<Instruction> callback) {
        this.instructionExpansionCallback = callback;
    }



    private void setupInstructionsTable() {
        TableColumn<Instruction, Number> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        numberCol.setSortable(false);
        numberCol.setPrefWidth(50);  // Fixed narrow width for line numbers
        numberCol.setMaxWidth(60);
        numberCol.setMinWidth(40);

        TableColumn<Instruction, String> typeCol = new TableColumn<>("B\\S");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeCol.setSortable(false);
        typeCol.setPrefWidth(60);   // Fixed narrow width for type
        typeCol.setMaxWidth(80);
        typeCol.setMinWidth(50);

        TableColumn<Instruction, Number> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        cyclesCol.setSortable(false);
        cyclesCol.setPrefWidth(80);  // Fixed moderate width for cycles
        cyclesCol.setMaxWidth(100);
        cyclesCol.setMinWidth(60);

        TableColumn<Instruction, String> descCol = new TableColumn<>("Instruction");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setSortable(false);
        // Let this column take up the remaining space
        descCol.setPrefWidth(400);  // Default width
        descCol.setMinWidth(200);   // Minimum readable width
        // Don't set maxWidth so it can grow as needed

        instructionsTable.getColumns().setAll(numberCol, typeCol, cyclesCol, descCol);
        instructionsTable.setItems(instructions);

        // Set the table to use computed column sizes
        instructionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Set up combined row factory that handles both highlighting AND double-click
        setupInstructionsRowFactory();
    }


    private void setupVariablesTable() {
        TableColumn<Variable, String> varNameCol = new TableColumn<>("Name");
        varNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        varNameCol.setSortable(false);
        varNameCol.setPrefWidth(100);  // Fixed width for variable names
        varNameCol.setMinWidth(80);
        varNameCol.setMaxWidth(150);

        TableColumn<Variable, Number> varValueCol = new TableColumn<>("Value");
        varValueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        varValueCol.setSortable(false);
        varValueCol.setPrefWidth(100);  // Fixed width for values
        varValueCol.setMinWidth(60);
        varValueCol.setMaxWidth(120);

        variablesTable.getColumns().setAll(varNameCol, varValueCol);
        variablesTable.setItems(variables);

        // Set the table to use computed column sizes
        variablesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Highlights all instructions in the instructions table that use the specified variable/label
     */
    private void highlightInstructionsUsingVariable(String variableName) {
        currentHighlightedVariable = variableName;

        // Refresh the table to apply the highlighting using the existing row factory
        instructionsTable.refresh();
    }

    /**
     * Clears any highlighting from the instructions table
     */
    private void clearInstructionHighlighting() {
        currentHighlightedVariable = null;

        // Refresh the table to remove the styling using the existing row factory
        instructionsTable.refresh();
    }

    /**
     * Checks if an instruction uses the specified variable/label
     */
    private boolean instructionUsesVariable(Instruction instruction, String variableName) {
        if (variableName == null || instruction == null) {
            return false;
        }

        String description = instruction.getDescription();
        if (description == null) {
            return false;
        }

        // Convert to lowercase for case-insensitive matching
        String lowerDescription = description.toLowerCase();
        String lowerVariableName = variableName.toLowerCase();

        boolean matches = false;

        // 1. Direct variable name match (word boundary to avoid partial matches)
        if (lowerDescription.matches(".*\\b" + java.util.regex.Pattern.quote(lowerVariableName) + "\\b.*")) {
            matches = true;
        }

        // 2. Variable in square brackets [variableName]
        if (lowerDescription.contains("[" + lowerVariableName + "]")) {
            matches = true;
        }

        // 3. Variable with register notation (e.g., R1, R2, etc.)
        if (lowerVariableName.matches("r\\d+") && lowerDescription.contains(lowerVariableName)) {
            matches = true;
        }

        // 4. Variable as memory address or operand
        if (lowerDescription.contains("(" + lowerVariableName + ")") ||
                lowerDescription.contains(lowerVariableName + ",") ||
                lowerDescription.contains("," + lowerVariableName) ||
                lowerDescription.contains(" " + lowerVariableName + " ")) {
            matches = true;
        }

        if (matches) {
            System.out.println("DEBUG: Found match in instruction: " + description + " for variable: " + variableName);
        }

        return matches;
    }

    /**
     * Public method to manually trigger highlighting for a specific variable
     * Can be called from other controllers if needed
     */
    public void highlightVariable(String variableName) {
        if (variableName == null || variableName.trim().isEmpty()) {
            clearHighlighting();
            return;
        }

        // Call the actual highlighting method
        highlightInstructionsUsingVariable(variableName.trim());
    }

    /**
     * Public method to clear highlighting
     */
    public void clearHighlighting() {
        clearInstructionHighlighting();
    }


    /**
     * Sets up the row factory for the instructions table that handles both highlighting and double-click
     */
    private void setupInstructionsRowFactory() {
        instructionsTable.setRowFactory(tv -> {
            TableRow<Instruction> row = new TableRow<Instruction>() {
                @Override
                protected void updateItem(Instruction instruction, boolean empty) {
                    super.updateItem(instruction, empty);

                    if (empty || instruction == null) {
                        setStyle("");
                    } else {
                        // Apply highlighting if a variable is selected
                        if (currentHighlightedVariable != null &&
                                instructionUsesVariable(instruction, currentHighlightedVariable)) {
                            setStyle("-fx-background-color: #FFFF99; -fx-text-fill: black;"); // Brighter yellow for dark theme
                        } else {
                            setStyle(""); // Clear any previous styling
                        }
                    }
                }
            };

            // Add double-click handler for instruction historical chain display
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Instruction selectedInstruction = row.getItem();
                    handleInstructionDoubleClick(selectedInstruction);
                }
            });

            return row;
        });
    }


    // Modify this method to work with the historical chain functionality
    private void handleInstructionDoubleClick(Instruction selectedInstruction) {
        try {
            // Call the EmulatorController's method to show instruction history
            if (instructionExpansionCallback != null) {
                instructionExpansionCallback.accept(selectedInstruction);
            }

            System.out.println("Double-clicked instruction #" + selectedInstruction.getNumber() +
                    ", requesting historical chain display");

        } catch (Exception e) {
            System.err.println("Error handling instruction double-click: " + e.getMessage());
            e.printStackTrace();
            // Clear the historical chain table on error
            historicalChainData.clear();
        }
    }



    private void setupHistoricalChainTable() {
        if (historicalChainTable == null) {
            System.err.println("Warning: historicalChainTable is null, skipping setup");
            return;
        }

        TableColumn<Instruction, Number> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        numberCol.setSortable(false);
        numberCol.setPrefWidth(50);  // Fixed narrow width for line numbers
        numberCol.setMaxWidth(60);
        numberCol.setMinWidth(40);

        TableColumn<Instruction, String> typeCol = new TableColumn<>("B\\S");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeCol.setSortable(false);
        typeCol.setPrefWidth(60);   // Fixed narrow width for type
        typeCol.setMaxWidth(80);
        typeCol.setMinWidth(50);

        TableColumn<Instruction, Number> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        cyclesCol.setSortable(false);
        cyclesCol.setPrefWidth(80);  // Fixed moderate width for cycles
        cyclesCol.setMaxWidth(100);
        cyclesCol.setMinWidth(60);

        TableColumn<Instruction, String> descCol = new TableColumn<>("Instruction");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setSortable(false);
        // Let this column take up the remaining space
        descCol.setPrefWidth(400);  // Default width
        descCol.setMinWidth(200);   // Minimum readable width

        historicalChainTable.getColumns().setAll(numberCol, typeCol, cyclesCol, descCol);
        historicalChainTable.setItems(historicalChainData);

        // Set the table to use computed column sizes
        historicalChainTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void displayHistoricalChain(Instruction selectedInstruction) {

        if (historicalChainTable == null) {
            return;
        }

        // Create a reversed copy to show most recent at top, oldest at bottom
        ObservableList<Instruction> reversed = FXCollections.observableArrayList();
        ObservableList<Instruction> chain = selectedInstruction.getHistoricalChain();

        // Add in reverse order - most recent (newest) first, oldest last
        for (int i = chain.size() - 1; i >= 0; i--) {
            reversed.add(chain.get(i));
        }

        historicalChainData.setAll(reversed);
    }

    private void clearHistoricalChain() {
        // Add null check to prevent NullPointerException
        if (historicalChainTable == null) {
            return;
        }

        historicalChainData.clear();
    }

    public TableView<Instruction> getHistoricalChainTable() {
        return historicalChainTable;
    }


    private void setupStatisticsTable() {
        TableColumn<Statistic, String> execTypeCol = new TableColumn<>("Execution Type");
        execTypeCol.setCellValueFactory(cellData -> cellData.getValue().executionTypeProperty());
        execTypeCol.setSortable(false);
        execTypeCol.setPrefWidth(200);  // Wider column for execution type
        execTypeCol.setMinWidth(150);
        execTypeCol.setMaxWidth(250);

        TableColumn<Statistic, Number> cyclesStatsCol = new TableColumn<>("Total Cycles");
        cyclesStatsCol.setCellValueFactory(cellData -> cellData.getValue().totalCyclesProperty());
        cyclesStatsCol.setSortable(false);
        cyclesStatsCol.setPrefWidth(150);  // Wider column for cycles
        cyclesStatsCol.setMinWidth(100);
        cyclesStatsCol.setMaxWidth(200);

        statisticsTable.getColumns().setAll(execTypeCol, cyclesStatsCol);
        statisticsTable.setItems(statistics);

        // Set the table to use computed column sizes for better distribution
        statisticsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // In TableController.java - update this method
    public void updateHistoricalChainTable(List<Instruction> chainInstructions) {
        System.out.println("=== DEBUG: TableController.updateHistoricalChainTable ===");
        System.out.println("Received " + (chainInstructions != null ? chainInstructions.size() : "null") + " instructions");
        System.out.println("historicalChainTable is " + (historicalChainTable != null ? "not null" : "null"));
        System.out.println("historicalChainData is " + (historicalChainData != null ? "not null" : "null"));

        if (historicalChainTable != null && historicalChainData != null) {
            historicalChainData.clear();
            System.out.println("Cleared existing data");

            if (chainInstructions != null) {
                historicalChainData.addAll(chainInstructions);
                System.out.println("Added " + chainInstructions.size() + " instructions to table data");
                System.out.println("historicalChainData now has " + historicalChainData.size() + " items");
            }
        } else {
            System.err.println("Cannot update table - table or data is null!");
        }

        System.out.println("=== END TableController.updateHistoricalChainTable ===");
    }

}

