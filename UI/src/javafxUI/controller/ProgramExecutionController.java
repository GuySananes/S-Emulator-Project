
package javafxUI.controller;

import core.logic.engine.Engine;
import core.logic.execution.ChangedVariable;
import core.logic.execution.DebugFinalResult;
import core.logic.execution.DebugResult;
import exception.NoProgramException;
import exception.ProgramNotExecutedYetException;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Modality;
import javafxUI.controller.dialog.InputDialog;
import javafxUI.model.ui.*;
import javafxUI.service.ModelConverter;
import present.program.PresentProgramDTO;
import run.DebugProgramDTO;
import run.ExecuteProgramDTO;
import run.ReExecuteProgramDTO;
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

    private DebugProgramDTO currentDebugSession;
    private boolean isDebugging = false;
    private int currentDebugIndex = -1;
    private List<DebugResult> debugHistory = new ArrayList<>();

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
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            // Get the ExecuteProgramDTO and extract the DebugProgramDTO
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            currentDebugSession = executeDTO.getDebugProgramDTO();

            // Get input values from user
            Optional<List<Long>> inputValues = getDebugInputValues(currentDebugSession);

            if (inputValues.isEmpty()) {
                updateSummary.accept("Debug session cancelled by user");
                return;
            }

            // Store inputs for potential step back operations
            lastExecutionInputs = new ArrayList<>(inputValues.get());

            // Set the input values
            currentDebugSession.setInput(inputValues.get());

            // Reset debug state
            isDebugging = true;
            currentDebugIndex = 0;
            debugHistory.clear();

            // Update UI for debug mode
            executionResult.setRunning(true);
            executionResult.setStatus("Debugging");
            executionResult.setCompleted(false);

            // Highlight first instruction
            highlightInstruction(currentDebugIndex);

            updateSummary.accept("Debug session started - press Step to execute instructions");

        } catch (Exception e) {
            showErrorDialog.accept("Debug Error", "Failed to start debugging: " + e.getMessage());
            updateSummary.accept("Failed to start debugging: " + e.getMessage());
        }
    }

    private Optional<List<Long>> getDebugInputValues(DebugProgramDTO debugDTO) {
        Set<core.logic.variable.Variable> requiredInputs = debugDTO.getOrderedInputVariables();

        InputDialog inputDialog = new InputDialog(requiredInputs);
        inputDialog.initOwner(startRegularButton.getScene().getWindow());
        inputDialog.initModality(Modality.WINDOW_MODAL);

        return inputDialog.showAndWait();
    }


    public void handleStop() {
        if (!isDebugging) {
            return;
        }

        // Reset debug state
        isDebugging = false;
        currentDebugSession = null;
        currentDebugIndex = -1;

        // Update UI
        executionResult.setRunning(false);
        executionResult.setStatus("Stopped");

        // Clear any instruction highlights
        clearInstructionHighlights();

        updateSummary.accept("Debug session stopped");
    }

    public void handleResume() {
        if (!isDebugging || currentDebugSession == null) {
            showErrorDialog.accept("Not Debugging", "No active debug session to resume.");
            return;
        }

        try {
            // Run until end
            DebugFinalResult result = currentDebugSession.runUntilEnd();

            // Update UI
            executionResult.setRunning(false);
            executionResult.setCompleted(true);
            executionResult.setStatus("Completed");
            executionResult.setCycles(result.getCycles());

            // Update variables with final values - use the new method
            updateVariablesWithDebugResults(currentDebugSession);

            // Add to execution history
            executionResult.addToHistory("Result: " + result.getResult());
            executionResult.addToHistory("Debug execution completed in " + result.getCycles() + " cycles");

            // Reset debug state
            isDebugging = false;

            updateSummary.accept("Debug execution completed - Result: " + result.getResult() +
                    ", Cycles: " + result.getCycles());

        } catch (Exception e) {
            handleExecutionFailure(e);
        }
    }


    public void handleStepOver() {
        if (!isDebugging || currentDebugSession == null) {
            showErrorDialog.accept("Not Debugging", "No active debug session to step through.");
            return;
        }

        try {
            // Execute the next instruction
            DebugResult result = currentDebugSession.nextStep();

            // Store the result for potential step back
            debugHistory.add(result);

            // Update the current index
            currentDebugIndex = result.getNextIndex();

            // Update cycles in UI
            executionResult.setCycles(result.getCycles());

            // Highlight the next instruction
            highlightInstruction(currentDebugIndex);

            // If there's a changed variable, update it in the UI
            ChangedVariable changed = result.getChangedVariable();
            if (changed != null) {
                updateChangedVariable(changed);
                updateSummary.accept("Stepped to instruction " + currentDebugIndex +
                        " - Variable " + changed.getVariable().getRepresentation() +
                        " changed from " + changed.getOldValue() + " to " + changed.getNewValue());
            } else {
                updateSummary.accept("Stepped to instruction " + currentDebugIndex);
            }

            // Check if we've reached the end
            if (currentDebugIndex == -1) {
                executionResult.setCompleted(true);
                executionResult.setRunning(false);
                executionResult.setStatus("Completed");
                updateSummary.accept("Debug execution completed");
                isDebugging = false;
            }

        } catch (Exception e) {
            showErrorDialog.accept("Debug Error", "Error during step execution: " + e.getMessage());
            updateSummary.accept("Step execution failed: " + e.getMessage());
        }
    }

    public void handleStepBack() {
        if (!isDebugging || debugHistory.isEmpty()) {
            showErrorDialog.accept("Cannot Step Back", "No previous steps to return to.");
            return;
        }

        try {
            // Remove the last step from history
            debugHistory.remove(debugHistory.size() - 1);

            // We need to restart the debug session and replay up to the previous point
            if (!debugHistory.isEmpty()) {
                // Get the last result to determine where we should be
                DebugResult lastResult = debugHistory.get(debugHistory.size() - 1);

                // Restart debug session
                ExecuteProgramDTO executeDTO = engine.executeProgram();
                currentDebugSession = executeDTO.getDebugProgramDTO();

                // Use the same inputs as before
                currentDebugSession.setInput(lastExecutionInputs);

                // Replay all steps up to the last one
                for (int i = 0; i < debugHistory.size(); i++) {
                    currentDebugSession.nextStep();
                }

                // Update current index
                currentDebugIndex = lastResult.getNextIndex();

                // Update the variables display to reflect the current state
                updateVariablesWithDebugResults(currentDebugSession);

                // Highlight the current instruction
                highlightInstruction(currentDebugIndex);

                // If there was a changed variable in the last step, update the UI to show it
                ChangedVariable changed = lastResult.getChangedVariable();
                if (changed != null) {
                    updateSummary.accept("Stepped back to instruction " + currentDebugIndex +
                            " - Variable " + changed.getVariable().getRepresentation() +
                            " reverted from " + changed.getNewValue() + " to " + changed.getOldValue());
                } else {
                    updateSummary.accept("Stepped back to instruction " + currentDebugIndex);
                }

            } else {
                // If no more history, restart from beginning
                handleStartDebug();
            }

        } catch (Exception e) {
            showErrorDialog.accept("Step Back Error", "Failed to step back: " + e.getMessage());
            updateSummary.accept("Failed to step back: " + e.getMessage());
        }
    }


    // Helper methods
    private void highlightInstruction(int index) {
        // Clear any existing highlights
        clearInstructionHighlights();

        // Highlight the current instruction if valid
        if (index >= 0 && index < instructions.size()) {
            Instruction current = instructions.get(index);
            // Mark the instruction as highlighted (similar to TableController approach)
            current.setHighlighted(true); // Assuming this method exists in Instruction

            // Update the UI to reflect this change
            executionResult.addToHistory("Highlighting instruction: " + current.getNumber());
        }
    }

    private void clearInstructionHighlights() {
        for (Instruction instruction : instructions) {
            // Clear highlight state on all instructions
            instruction.setHighlighted(false); // Assuming this method exists
        }
    }

    private void updateChangedVariable(ChangedVariable changed) {
        String variableName = changed.getVariable().getRepresentation();

        for (Variable var : variables) {
            // First, clear any previous highlighting
            var.setHighlighted(false);

            if (var.getName().equals(variableName)) {
                var.setValue((int) changed.getNewValue());
                // Mark this variable as highlighted
                var.setHighlighted(true); // Assuming this method exists

                // Add to execution history for visibility
                executionResult.addToHistory("Variable " + variableName +
                        " changed: " + changed.getOldValue() + " → " + changed.getNewValue());
                break;
            }
        }
    }

    // Add a new method specifically for DebugProgramDTO
    private void updateVariablesWithDebugResults(DebugProgramDTO debugDTO) {
        variables.clear();

        Set<core.logic.variable.Variable> programVariables = debugDTO.getOrderedVariables();
        List<Long> variableValues = debugDTO.getOrderedValues();

        if (programVariables != null && variableValues != null) {
            List<core.logic.variable.Variable> orderedVars = new ArrayList<>(programVariables);

            for (int i = 0; i < Math.min(orderedVars.size(), variableValues.size()); i++) {
                core.logic.variable.Variable engineVar = orderedVars.get(i);
                Long value = variableValues.get(i);

                variables.add(new Variable(
                        engineVar.getRepresentation(),
                        value.intValue(),
                        engineVar.getType().name()
                ));
            }
        }

        executionResult.addToHistory("Variables updated with debug results");
    }



    public void handleRerun() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            // Get program statistics to determine available run numbers
            ProgramStatisticsDTO statsDTO = engine.presentProgramStats();
            List<SingleRunStatisticDTO> programStats = statsDTO.getProgramStatisticCopy();

            if (programStats.isEmpty()) {
                showErrorDialog.accept("No Previous Runs", "No previous executions found. Please run the program first.");
                return;
            }

            // Show run selection dialog
            Optional<Integer> selectedRunNumber = showRunSelectionDialog(programStats);

            if (selectedRunNumber.isEmpty()) {
                updateSummary.accept("Rerun cancelled by user");
                return;
            }

            int runNumber = selectedRunNumber.get();

            // Use reExecuteProgram to get the ReExecuteProgramDTO
            ReExecuteProgramDTO reExecuteDTO = engine.reExecuteProgram(runNumber);

            // Get the DTOs using the getters
            PresentProgramDTO presentDTO = reExecuteDTO.getPresentProgramDTO();
            ExecuteProgramDTO executeDTO = reExecuteDTO.getExecuteProgramDTO();

            // Update the UI with the program presentation
            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(presentDTO));

            variables.clear();
            variables.addAll(ModelConverter.convertVariables(presentDTO));

            // Get the RunProgramDTO which already has the inputs pre-configured from reExecuteProgram
            RunProgramDTO runDTO = executeDTO.getRunProgramDTO();

            // The reExecuteProgram method already configured the correct inputs,
            // but we want to give the user a chance to modify them.
            // Since we can't easily extract the pre-configured inputs, we'll execute as-is.

            // Execute the program with the pre-configured inputs
            core.logic.execution.ResultCycle result = runDTO.runProgram();

            // Update UI after execution
            updateUIAfterExecution(runDTO, result);

            updateSummary.accept("Rerun of execution #" + runNumber + " completed successfully - " +
                    "Result: " + result.getResult() + ", Cycles: " + result.getCycles());

        } catch (NoProgramException e) {
            showErrorDialog.accept("No Program", "Please load a program first.");
        } catch (ProgramNotExecutedYetException e) {
            showErrorDialog.accept("No Previous Executions", "No previous executions found. Please run the program first.");
        } catch (Exception e) {
            showErrorDialog.accept("Rerun Error", "Failed to rerun program: " + e.getMessage());
            updateSummary.accept("Rerun failed: " + e.getMessage());
        }
    }

    private Optional<Integer> showRunSelectionDialog(List<SingleRunStatisticDTO> programStats) {
        List<String> runOptions = new ArrayList<>();
        for (SingleRunStatisticDTO stat : programStats) {
            runOptions.add("Run #" + stat.getRunNumber() +
                    " (Degree: " + stat.getRunDegree() +
                    ", Cycles: " + stat.getCycles() +
                    ", Result: " + stat.getResult() + ")");
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(runOptions.get(0), runOptions);
        dialog.setTitle("Select Run to Re-execute");
        dialog.setHeaderText("Choose which run to re-execute:");
        dialog.setContentText("Available runs:");

        // Set the owner and modality like other dialogs
        dialog.initOwner(startRegularButton.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);

        Optional<String> result = dialog.showAndWait();

        return result.map(selectedOption -> {
            int runIndex = runOptions.indexOf(selectedOption);
            return programStats.get(runIndex).getRunNumber();
        });
    }

    private Optional<List<Long>> showInputDialogWithDefaults(RunProgramDTO runDTO, List<Long> prefilledValues) {
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

            // Wrap the expansion in a try-catch to handle label lookup issues
            PresentProgramDTO expandedProgram;
            try {
                expandedProgram = engine.expandOrShrinkProgram(newDegree);
            } catch (java.util.NoSuchElementException e) {
                if (e.getMessage() != null && e.getMessage().contains("label")) {
                    showErrorDialog.accept("Expansion Error",
                            "Cannot expand program due to label reference issue: " + e.getMessage());
                    updateSummary.accept("Expansion failed: Label reference error");
                    return;
                }
                throw e; // Re-throw if it's not a label issue
            }

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

            // Wrap the collapse in a try-catch to handle label lookup issues
            PresentProgramDTO collapsedProgram;
            try {
                collapsedProgram = engine.expandOrShrinkProgram(newDegree);
            } catch (java.util.NoSuchElementException e) {
                if (e.getMessage() != null && e.getMessage().contains("label")) {
                    showErrorDialog.accept("Collapse Error",
                            "Cannot collapse program due to label reference issue: " + e.getMessage());
                    updateSummary.accept("Collapse failed: Label reference error");
                    return;
                }
                throw e; // Re-throw if it's not a label issue
            }

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