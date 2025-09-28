
package javafxUI.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafxUI.model.ui.Instruction;
import javafxUI.model.ui.Statistic;
import javafxUI.model.ui.Variable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

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

        TableColumn<Instruction, String> typeCol = new TableColumn<>("B\\S");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeCol.setSortable(false);

        TableColumn<Instruction, Number> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        cyclesCol.setSortable(false);

        TableColumn<Instruction, String> descCol = new TableColumn<>("Instruction");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setSortable(false);

        instructionsTable.getColumns().setAll(numberCol, typeCol, cyclesCol, descCol);
        instructionsTable.setItems(instructions);

        // Add selection listener to show historical chain
        instructionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.hasHistoricalChain()) {
                displayHistoricalChain(newSelection);
            } else {
                clearHistoricalChain();
            }
        });

        // Add double-click handler for instruction expansion
        instructionsTable.setRowFactory(tv -> {
            TableRow<Instruction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Instruction selectedInstruction = row.getItem();
                    if (instructionExpansionCallback != null && selectedInstruction != null) {
                        instructionExpansionCallback.accept(selectedInstruction);
                    }
                }
            });
            return row;
        });

        // Make instructions table 3 times taller
        instructionsTable.setPrefHeight(450);
        instructionsTable.setMinHeight(450);
    }

    private void setupVariablesTable() {
        TableColumn<Variable, String> varNameCol = new TableColumn<>("Name");
        varNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        varNameCol.setSortable(false);

        TableColumn<Variable, Number> varValueCol = new TableColumn<>("Value");
        varValueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        varValueCol.setSortable(false);

        variablesTable.getColumns().setAll(varNameCol, varValueCol);
        variablesTable.setItems(variables);
    }

    private void setupHistoricalChainTable() {
        if (historicalChainTable == null) {
            System.err.println("Warning: historicalChainTable is null, skipping setup");
            return;
        }

        TableColumn<Instruction, Number> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        numberCol.setSortable(false);

        TableColumn<Instruction, String> typeCol = new TableColumn<>("B\\S");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeCol.setSortable(false);

        TableColumn<Instruction, Number> cyclesCol = new TableColumn<>("Cycles");
        cyclesCol.setCellValueFactory(cellData -> cellData.getValue().cyclesProperty());
        cyclesCol.setSortable(false);

        TableColumn<Instruction, String> descCol = new TableColumn<>("Instruction");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setSortable(false);

        historicalChainTable.getColumns().setAll(numberCol, typeCol, cyclesCol, descCol);
        historicalChainTable.setItems(historicalChainData);
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

        TableColumn<Statistic, Number> cyclesStatsCol = new TableColumn<>("Total Cycles");
        cyclesStatsCol.setCellValueFactory(cellData -> cellData.getValue().totalCyclesProperty());
        cyclesStatsCol.setSortable(false);

        statisticsTable.getColumns().setAll(execTypeCol, cyclesStatsCol);
        statisticsTable.setItems(statistics);
    }
}