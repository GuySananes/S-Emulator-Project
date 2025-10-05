
package javafxUI.controller;

import core.logic.engine.Engine;
import exception.NoProgramException;
import exception.ProgramNotExecutedYetException;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafxUI.controller.dialog.InputDialog;
import javafxUI.model.ui.*;
import javafxUI.service.ModelConverter;
import present.program.PresentProgramDTO;
import run.ExecuteProgramDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticsDTO;
import statistic.SingleRunStatisticDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Handles all program execution and debugging operations
 */
public class ProgramExecutionController {

    private final Program currentProgram;
    private final ExecutionResult executionResult;
    private final ObservableList<Instruction> instructions;
    private final ObservableList<Variable> variables;

    private final Button startRegularButton;
    private final Button startDebugButton;
    private final Button stopButton;
    private final Button resumeButton;
    private final Button stepOverButton;
    private final Button stepBackButton;
    private final Button rerunButton;

    private final Consumer<String> updateSummary;
    private final BiConsumer<String, String> showErrorDialog;

    private final ObservableList<Statistic> statistics;

    private List<Long> lastExecutionInputs = new ArrayList<>();
    private int lastExecutionDegree = 0;

    private int currentDisplayDegree = 0;

    private Engine engine;

    public ProgramExecutionController(Program currentProgram,
                                      ExecutionResult executionResult,
                                      ObservableList<Instruction> instructions,
                                      ObservableList<Variable> variables,
                                      ObservableList<Statistic> statistics,
                                      Button startRegularButton, Button startDebugButton,
                                      Button stopButton, Button resumeButton,
                                      Button stepOverButton, Button stepBackButton,
                                      Button rerunButton,
                                      Consumer<String> updateSummary,
                                      BiConsumer<String, String> showErrorDialog) {
        this.currentProgram = currentProgram;
        this.executionResult = executionResult;
        this.instructions = instructions;
        this.variables = variables;
        this.statistics = statistics;

        this.startRegularButton = startRegularButton;
        this.startDebugButton = startDebugButton;
        this.stopButton = stopButton;
        this.resumeButton = resumeButton;
        this.stepOverButton = stepOverButton;
        this.stepBackButton = stepBackButton;
        this.rerunButton = rerunButton;

        this.updateSummary = updateSummary;
        this.showErrorDialog = showErrorDialog;

        this.engine = Engine.getInstance();
    }

    public void setupEventHandlers() {
        startRegularButton.setOnAction(e -> handleStartRegular());
        startDebugButton.setOnAction(e -> handleStartDebug());
        stopButton.setOnAction(e -> handleStop());
        resumeButton.setOnAction(e -> handleResume());
        stepOverButton.setOnAction(e -> handleStepOver());
        stepBackButton.setOnAction(e -> handleStepBack());
        rerunButton.setOnAction(e -> handleRerun());
    }

    public void handleStartRegular() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            RunProgramDTO runDTO = executeDTO.getRunProgramDTO();

            Optional<List<Long>> inputValues = getInputValues(runDTO);

            if (inputValues.isEmpty()) {
                updateSummary.accept("Execution cancelled by user");
                return;
            }

            runDTO.setInput(inputValues.get());
            executeProgram(runDTO, inputValues.get());

        } catch (Exception e) {
            showErrorDialog.accept("Execution Error", "Failed to start execution: " + e.getMessage());
            updateSummary.accept("Failed to start execution: " + e.getMessage());
        }
    }

    private Optional<List<Long>> getInputValues(RunProgramDTO runDTO) {
        Set<core.logic.variable.Variable> requiredInputs = runDTO.getOrderedInputVariables();

        InputDialog inputDialog = new InputDialog(requiredInputs);
        inputDialog.initOwner(startRegularButton.getScene().getWindow());
        inputDialog.initModality(Modality.WINDOW_MODAL);

        return inputDialog.showAndWait();
    }

    private void executeProgram(RunProgramDTO runDTO, List<Long> inputValues) {
        try {
            this.lastExecutionInputs = new ArrayList<>(inputValues);
            this.lastExecutionDegree = currentDisplayDegree;

            runDTO.setInput(inputValues);
            core.logic.execution.ResultCycle result = runDTO.runProgram();

            // FIXED: Pass the same runDTO that was executed to get the correct variable values
            updateUIAfterExecution(runDTO, result);
            updateSummary.accept("Program executed successfully - Result: " + result.getResult() +
                    ", Cycles: " + result.getCycles() +
                    (currentDisplayDegree > 0 ? " (degree " + currentDisplayDegree + ")" : ""));

        } catch (Exception e) {
            handleExecutionFailure(e);
        }
    }

    private void updateUIAfterExecution(RunProgramDTO runDTO, core.logic.execution.ResultCycle result) {
        try {
            executionResult.setCompleted(true);
            executionResult.setRunning(false);
            executionResult.setStatus("Completed");
            executionResult.setCycles((int) result.getCycles());

            // FIXED: Use the runDTO that was actually executed to get variable values
            updateVariablesWithResults(runDTO);

            updateSummary.accept("Execution completed - Result: " + result.getResult() + ", Cycles: " + result.getCycles());
            executionResult.addToHistory("Result: " + result.getResult());
            executionResult.addToHistory("Execution Cycles: " + result.getCycles());

        } catch (Exception e) {
            showErrorDialog.accept("Execution Error", "Error updating UI: " + e.getMessage());
        }
    }

    private void updateVariablesWithResults(RunProgramDTO runDTO) {
        variables.clear();

        // FIXED: Get variables and values from the SAME runDTO that was executed
        Set<core.logic.variable.Variable> programVariables = runDTO.getOrderedVariables();
        List<Long> variableValues = runDTO.getOrderedValues();

        System.out.println("=== DEBUG: updateVariablesWithResults ===");
        System.out.println("Number of variables: " + (programVariables != null ? programVariables.size() : "null"));
        System.out.println("Number of values: " + (variableValues != null ? variableValues.size() : "null"));

        if (programVariables != null && variableValues != null) {
            List<core.logic.variable.Variable> orderedVars = new ArrayList<>(programVariables);

            for (int i = 0; i < Math.min(orderedVars.size(), variableValues.size()); i++) {
                core.logic.variable.Variable engineVar = orderedVars.get(i);
                Long value = variableValues.get(i);

                System.out.println("Variable[" + i + "]: " + engineVar.getRepresentation() + " = " + value);

                variables.add(new Variable(
                        engineVar.getRepresentation(),
                        value.intValue(),
                        engineVar.getType().name()
                ));
            }
        }

        System.out.println("=== END updateVariablesWithResults ===");
        executionResult.addToHistory("Variables updated with execution results");
    }

    private void handleExecutionFailure(Throwable exception) {
        showErrorDialog.accept("Execution Error", "Execution failed: " + exception.getMessage());
        executionResult.setRunning(false);
        executionResult.setStatus("Failed");
        updateSummary.accept("Execution failed: " + exception.getMessage());
    }

    public void handleStartDebug() {
        updateSummary.accept("Debug mode not fully implemented yet");
    }

    public void handleStop() {
        executionResult.setRunning(false);
        executionResult.setStatus("Stopped");
        updateSummary.accept("Execution stopped");
    }

    public void handleResume() {
        updateSummary.accept("Execution resumed");
    }

    public void handleStepOver() {
        updateSummary.accept("Stepped over instruction");
    }

    public void handleStepBack() {
        updateSummary.accept("Stepped back instruction");
    }

    public void handleRerun() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        if (lastExecutionInputs == null || lastExecutionInputs.isEmpty()) {
            showErrorDialog.accept("No Previous Run",
                    "No previous execution found. Please run the program first.");
            return;
        }

        try {
            // Restore to last execution degree if needed
            if (lastExecutionDegree != currentDisplayDegree) {
                PresentProgramDTO programPresent = engine.expandOrShrinkProgram(lastExecutionDegree);
                instructions.clear();
                instructions.addAll(ModelConverter.convertInstructions(programPresent));
                currentDisplayDegree = lastExecutionDegree;
                currentProgram.setCurrentDegree(lastExecutionDegree);
            }

            ExecuteProgramDTO executeDTO = engine.executeProgram();
            RunProgramDTO runDTO = executeDTO.getRunProgramDTO();

            Optional<List<Long>> inputValues = getInputValuesForRerun(runDTO, lastExecutionInputs);

            if (inputValues.isEmpty()) {
                updateSummary.accept("Rerun cancelled by user");
                return;
            }

            lastExecutionInputs = new ArrayList<>(inputValues.get());
            lastExecutionDegree = currentDisplayDegree;

            runDTO.setInput(inputValues.get());
            executeProgram(runDTO, inputValues.get());

            updateSummary.accept("Rerun completed with " +
                    (lastExecutionDegree > 0 ? "degree " + lastExecutionDegree : "original program"));

        } catch (Exception e) {
            showErrorDialog.accept("Rerun Error", "Failed to rerun program: " + e.getMessage());
            updateSummary.accept("Rerun failed: " + e.getMessage());
        }
    }

    private Optional<List<Long>> getInputValuesForRerun(RunProgramDTO runDTO, List<Long> prefilledValues) {
        Set<core.logic.variable.Variable> requiredInputs = runDTO.getOrderedInputVariables();

        InputDialog inputDialog = new InputDialog(requiredInputs, prefilledValues);
        inputDialog.initOwner(startRegularButton.getScene().getWindow());
        inputDialog.initModality(Modality.WINDOW_MODAL);

        return inputDialog.showAndWait();
    }

    public void handleExpand() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            int maxDegree = currentProgram.getMaxDegree();

            if (maxDegree == 0) {
                showErrorDialog.accept("Cannot Expand", "The currently selected program cannot be expanded.");
                return;
            }

            if (currentDisplayDegree >= maxDegree) {
                updateSummary.accept("Already at maximum expansion degree (" + maxDegree + ") for " + currentProgram.getName());
                return;
            }

            int newDegree = currentDisplayDegree + 1;
            PresentProgramDTO expandedProgram = engine.expandOrShrinkProgram(newDegree);

            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(expandedProgram));

            // Update variables too in case they changed
            variables.clear();
            variables.addAll(ModelConverter.convertVariables(expandedProgram));

            currentDisplayDegree = newDegree;
            currentProgram.setCurrentDegree(newDegree);

            updateSummary.accept("Expanded '" + currentProgram.getName() + "' to degree " + newDegree);

        } catch (Exception e) {
            showErrorDialog.accept("Expansion Error", "Failed to expand program: " + e.getMessage());
            updateSummary.accept("Expansion failed: " + e.getMessage());
        }
    }

    public void handleCollapse() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            if (currentDisplayDegree <= 0) {
                updateSummary.accept("Already at minimum degree (0) for " + currentProgram.getName());
                return;
            }

            int newDegree = currentDisplayDegree - 1;
            PresentProgramDTO collapsedProgram = engine.expandOrShrinkProgram(newDegree);

            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(collapsedProgram));

            // Update variables too in case they changed
            variables.clear();
            variables.addAll(ModelConverter.convertVariables(collapsedProgram));

            currentDisplayDegree = newDegree;
            currentProgram.setCurrentDegree(newDegree);

            if (newDegree == 0) {
                updateSummary.accept("Showing original '" + currentProgram.getName() + "' (degree 0)");
            } else {
                updateSummary.accept("Collapsed '" + currentProgram.getName() + "' to degree " + newDegree);
            }

        } catch (Exception e) {
            showErrorDialog.accept("Collapse Error", "Failed to collapse program: " + e.getMessage());
            updateSummary.accept("Collapse failed: " + e.getMessage());
        }
    }

    // Add method to reset display degree when switching programs
    public void resetDisplayDegree() {
        this.currentDisplayDegree = 0;
    }

    public void handleHighlight() {
        updateSummary.accept("Instructions highlighted");
    }

    public void handleShowStats() {
        try {
            ProgramStatisticsDTO statsDTO = engine.presentProgramStats();

            List<SingleRunStatisticDTO> programStats = statsDTO.getProgramStatisticCopy();

            statistics.clear();

            for (SingleRunStatisticDTO runStat : programStats) {
                Statistic uiStatistic = new Statistic(
                        "Run #" + runStat.getRunNumber() + " (Degree: " + runStat.getRunDegree() + ")",
                        (int) runStat.getCycles(),
                        0,
                        formatExecutionDetails(runStat)
                );

                statistics.add(uiStatistic);
            }

            addSummaryStatistics(statistics, programStats);

            updateSummary.accept("Statistics displayed - " + programStats.size() + " program runs shown.");

        } catch (NoProgramException e) {
            showErrorDialog.accept("No Program", "Please load a program first.");
        } catch (ProgramNotExecutedYetException e) {
            showErrorDialog.accept("No Statistics", "Please run the program first to see statistics.");
        } catch (Exception e) {
            showErrorDialog.accept("Statistics Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    private void addSummaryStatistics(ObservableList<Statistic> statisticsList, List<SingleRunStatisticDTO> programStats) {
        if (programStats.isEmpty()) return;

        long totalCycles = programStats.stream().mapToLong(SingleRunStatisticDTO::getCycles).sum();
        double avgCycles = (double) totalCycles / programStats.size();
        //long maxCycles = programStats.stream().mapToLong(SingleRunStatisticDTO::getCycles).max().orElse(0);
        long cycles = programStats.stream().mapToLong(SingleRunStatisticDTO::getCycles).min().orElse(0);

        statisticsList.add(new Statistic("=== SUMMARY ===", 0, 0, ""));
        statisticsList.add(new Statistic("Total Runs", programStats.size(), 0, ""));
        statisticsList.add(new Statistic("Total Cycles", (int) totalCycles, 0, ""));
        statisticsList.add(new Statistic("Average Cycles", (int) Math.round(avgCycles), 0, String.format("%.1f per run", avgCycles)));
        statisticsList.add(new Statistic("last run number of Cycles", (int) cycles, 0, "Best performance"));
    }

    private String formatExecutionDetails(SingleRunStatisticDTO runStat) {
        StringBuilder details = new StringBuilder();
        details.append("Result: ").append(runStat.getResult());
        details.append(", ").append(runStat.getRepresentation());
        return details.toString();
    }
}