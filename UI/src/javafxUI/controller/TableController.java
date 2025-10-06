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
import java.util.function.Consumer;

/**
 * Handles all table setup and configuration
 */
public class TableController {

    private final TableView<Instruction> instructionsTable;
    private final TableView<Variable> variablesTable;
    private final TableView<Statistic> statisticsTable;
    private final TableView<Instruction> historicalChainTable;

    private final ObservableList<Instruction> historicalChainData = FXCollections.observableArrayList();
    private final ObservableList<Instruction> instructions;
    private final ObservableList<Variable> variables;
    private final ObservableList<Statistic> statistics;

    private Consumer<Instruction> instructionExpansionCallback;
    private String currentHighlightedVariable = null;
    private Instruction currentDebugInstruction = null;

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

    public void setInstructionExpansionCallback(Consumer<Instruction> callback) {
        this.instructionExpansionCallback = callback;
    }

    // ==================== INSTRUCTIONS TABLE ====================

    private void setupInstructionsTable() {
        TableColumn<Instruction, Number> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        numberCol.setSortable(false);
        numberCol.setPrefWidth(50);
        numberCol.setMaxWidth(60);
        numberCol.setMinWidth(40);

        TableColumn<Instruction, String> typeCol = new TableColumn<>("B\\S");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeCol.setSortable(false);
        typeCol.setPrefWidth(60);
        typeCol.setMaxWidth(80);
        typeCol.setMinWidth(50);

        TableColumn<Instruction, Number> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        cyclesCol.setSortable(false);
        cyclesCol.setPrefWidth(80);
        cyclesCol.setMaxWidth(100);
        cyclesCol.setMinWidth(60);

        TableColumn<Instruction, String> descCol = new TableColumn<>("Instruction");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setSortable(false);
        descCol.setPrefWidth(400);
        descCol.setMinWidth(200);

        instructionsTable.getColumns().setAll(numberCol, typeCol, cyclesCol, descCol);
        instructionsTable.setItems(instructions);
        instructionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupInstructionsRowFactory();
    }

    private void setupInstructionsRowFactory() {
        instructionsTable.setRowFactory(tv -> {
            TableRow<Instruction> row = new TableRow<Instruction>() {
                @Override
                protected void updateItem(Instruction instruction, boolean empty) {
                    super.updateItem(instruction, empty);

                    if (empty || instruction == null) {
                        setStyle("");
                        getStyleClass().removeAll("highlighted-row", "current-instruction-row");
                    } else {
                        // Check if this is the current debug instruction by object reference
                        boolean isCurrent = (currentDebugInstruction != null &&
                                currentDebugInstruction == instruction);

                        System.out.println("Row update - Instruction: " + instruction.getNumber() +
                                ", isCurrent: " + isCurrent +
                                ", currentDebugInstruction: " +
                                (currentDebugInstruction != null ? currentDebugInstruction.getNumber() : "null"));

                        if (isCurrent) {
                            getStyleClass().removeAll("highlighted-row");
                            if (!getStyleClass().contains("current-instruction-row")) {
                                getStyleClass().add("current-instruction-row");
                                System.out.println("  -> Added current-instruction-row class to instruction " + instruction.getNumber());
                            }
                        } else {
                            getStyleClass().removeAll("current-instruction-row");

                            if (currentHighlightedVariable != null &&
                                    instructionUsesVariable(instruction, currentHighlightedVariable)) {
                                if (!getStyleClass().contains("highlighted-row")) {
                                    getStyleClass().add("highlighted-row");
                                }
                            } else {
                                getStyleClass().removeAll("highlighted-row");
                            }
                        }
                    }
                }
            };

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleInstructionDoubleClick(row.getItem());
                }
            });

            return row;
        });
    }

    private void handleInstructionDoubleClick(Instruction selectedInstruction) {
        try {
            if (instructionExpansionCallback != null) {
                instructionExpansionCallback.accept(selectedInstruction);
            }
        } catch (Exception e) {
            System.err.println("Error handling instruction double-click: " + e.getMessage());
            e.printStackTrace();
            historicalChainData.clear();
        }
    }

    // ==================== VARIABLES TABLE ====================

    private void setupVariablesTable() {
        TableColumn<Variable, String> varNameCol = new TableColumn<>("Name");
        varNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        varNameCol.setSortable(false);
        varNameCol.setPrefWidth(100);
        varNameCol.setMinWidth(80);
        varNameCol.setMaxWidth(150);

        TableColumn<Variable, Number> varValueCol = new TableColumn<>("Value");
        varValueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        varValueCol.setSortable(false);
        varValueCol.setPrefWidth(100);
        varValueCol.setMinWidth(60);
        varValueCol.setMaxWidth(120);

        variablesTable.getColumns().setAll(varNameCol, varValueCol);
        variablesTable.setItems(variables);
        variablesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ==================== STATISTICS TABLE ====================

    private void setupStatisticsTable() {
        TableColumn<Statistic, String> execTypeCol = new TableColumn<>("Execution Type");
        execTypeCol.setCellValueFactory(cellData -> cellData.getValue().executionTypeProperty());
        execTypeCol.setSortable(false);
        execTypeCol.setPrefWidth(200);
        execTypeCol.setMinWidth(150);
        execTypeCol.setMaxWidth(250);

        TableColumn<Statistic, Number> cyclesStatsCol = new TableColumn<>("Total Cycles");
        cyclesStatsCol.setCellValueFactory(cellData -> cellData.getValue().totalCyclesProperty());
        cyclesStatsCol.setSortable(false);
        cyclesStatsCol.setPrefWidth(150);
        cyclesStatsCol.setMinWidth(100);
        cyclesStatsCol.setMaxWidth(200);

        statisticsTable.getColumns().setAll(execTypeCol, cyclesStatsCol);
        statisticsTable.setItems(statistics);
        statisticsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ==================== HISTORICAL CHAIN TABLE ====================

    private void setupHistoricalChainTable() {
        if (historicalChainTable == null) {
            System.err.println("Warning: historicalChainTable is null");
            return;
        }

        TableColumn<Instruction, Number> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        numberCol.setSortable(false);
        numberCol.setPrefWidth(50);
        numberCol.setMaxWidth(60);
        numberCol.setMinWidth(40);

        TableColumn<Instruction, String> typeCol = new TableColumn<>("B\\S");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeCol.setSortable(false);
        typeCol.setPrefWidth(60);
        typeCol.setMaxWidth(80);
        typeCol.setMinWidth(50);

        TableColumn<Instruction, Number> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        cyclesCol.setSortable(false);
        cyclesCol.setPrefWidth(80);
        cyclesCol.setMaxWidth(100);
        cyclesCol.setMinWidth(60);

        TableColumn<Instruction, String> descCol = new TableColumn<>("Instruction");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setSortable(false);
        descCol.setPrefWidth(400);
        descCol.setMinWidth(200);

        historicalChainTable.getColumns().setAll(numberCol, typeCol, cyclesCol, descCol);
        historicalChainTable.setItems(historicalChainData);
        historicalChainTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void updateHistoricalChainTable(List<Instruction> chainInstructions) {
        if (historicalChainTable != null && historicalChainData != null) {
            historicalChainData.clear();
            if (chainInstructions != null) {
                historicalChainData.addAll(chainInstructions);
            }
        }
    }

    // ==================== HIGHLIGHTING METHODS ====================

    public void highlightVariable(String variableName) {
        if (variableName == null || variableName.trim().isEmpty()) {
            clearHighlighting();
            return;
        }
        currentHighlightedVariable = variableName.trim();
        instructionsTable.refresh();
    }

    public void clearHighlighting() {
        currentHighlightedVariable = null;
        instructionsTable.refresh();
    }

    /**
     * Highlights the current instruction during debugging by index in the observable list
     */
    public void highlightCurrentInstruction(int index) {
        // Get the actual instruction object from the list at the given index
        if (index >= 0 && index < instructions.size()) {
            currentDebugInstruction = instructions.get(index);
            System.out.println("DEBUG: Highlighting instruction at index " + index +
                    " (line #" + currentDebugInstruction.getNumber() + "): " +
                    currentDebugInstruction.getDescription());
        } else {
            currentDebugInstruction = null;
            System.out.println("DEBUG: Index " + index + " out of bounds, clearing highlight");
        }
        instructionsTable.refresh();
    }

    public void clearCurrentInstructionHighlight() {
        currentDebugInstruction = null;
        instructionsTable.refresh();
    }

    private boolean instructionUsesVariable(Instruction instruction, String variableName) {
        if (variableName == null || instruction == null) {
            return false;
        }

        String description = instruction.getDescription();
        if (description == null) {
            return false;
        }

        String lowerDescription = description.toLowerCase();
        String lowerVariableName = variableName.toLowerCase();

        if (lowerDescription.matches(".*\\b" + java.util.regex.Pattern.quote(lowerVariableName) + "\\b.*")) {
            return true;
        }

        if (lowerDescription.contains("[" + lowerVariableName + "]")) {
            return true;
        }

        if (lowerVariableName.matches("r\\d+") && lowerDescription.contains(lowerVariableName)) {
            return true;
        }

        if (lowerDescription.contains("(" + lowerVariableName + ")") ||
                lowerDescription.contains(lowerVariableName + ",") ||
                lowerDescription.contains("," + lowerVariableName) ||
                lowerDescription.contains(" " + lowerVariableName + " ")) {
            return true;
        }

        return false;
    }

    public TableView<Instruction> getHistoricalChainTable() {
        return historicalChainTable;
    }
}