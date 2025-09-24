
package javafx.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.model.ui.Instruction;
import javafx.model.ui.Variable;
import javafx.model.ui.Statistic;

/**
 * Handles all table setup and configuration
 */
public class TableController {

    private final TableView<Instruction> instructionsTable;
    private final TableView<Variable> variablesTable;
    private final TableView<Statistic> statisticsTable;

    private final ObservableList<Instruction> instructions;
    private final ObservableList<Variable> variables;
    private final ObservableList<Statistic> statistics;

    public TableController(TableView<Instruction> instructionsTable,
                           TableView<Variable> variablesTable,
                           TableView<Statistic> statisticsTable,
                           ObservableList<Instruction> instructions,
                           ObservableList<Variable> variables,
                           ObservableList<Statistic> statistics) {
        this.instructionsTable = instructionsTable;
        this.variablesTable = variablesTable;
        this.statisticsTable = statisticsTable;
        this.instructions = instructions;
        this.variables = variables;
        this.statistics = statistics;
    }

    public void setupAllTables() {
        setupInstructionsTable();
        setupVariablesTable();
        setupStatisticsTable();
    }

    private void setupInstructionsTable() {
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
        instructionsTable.setPrefHeight(450);
        instructionsTable.setMinHeight(450);
    }

    private void setupVariablesTable() {
        TableColumn<Variable, String> varNameCol = new TableColumn<>("Name");
        varNameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Variable, Number> varValueCol = new TableColumn<>("Value");
        varValueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty());

        variablesTable.getColumns().setAll(varNameCol, varValueCol);
        variablesTable.setItems(variables);
    }

    private void setupStatisticsTable() {
        TableColumn<Statistic, String> execTypeCol = new TableColumn<>("Execution Type");
        execTypeCol.setCellValueFactory(cellData -> cellData.getValue().executionTypeProperty());

        TableColumn<Statistic, Number> cyclesStatsCol = new TableColumn<>("Total Cycles");
        cyclesStatsCol.setCellValueFactory(cellData -> cellData.getValue().totalCyclesProperty());

        statisticsTable.getColumns().setAll(execTypeCol, cyclesStatsCol);
        statisticsTable.setItems(statistics);
    }
}