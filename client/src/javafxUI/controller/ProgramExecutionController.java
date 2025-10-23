package javafxUI.controller;

import core.logic.engine.Engine;
import core.logic.execution.ChangedVariable;
import core.logic.execution.DebugFinalResult;
import core.logic.execution.DebugResult;
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
    private final ObservableList<Statistic> statistics;

    private final Button startRegularButton;
    private final Button startDebugButton;
    private final Button stopButton;
    private final Button resumeButton;
    private final Button stepOverButton;
    private final Button stepBackButton;
    private final Button rerunButton;

    private final Consumer<String> updateSummary;
    private final BiConsumer<String, String> showErrorDialog;

    private DebugProgramDTO currentDebugSession;
    private boolean isDebugging = false;
    private int currentDebugIndex = -1;
    private List<DebugResult> debugHistory = new ArrayList<>();
    private List<Long> lastExecutionInputs = new ArrayList<>();
    private int currentDisplayDegree = 0;

    private Engine engine;
    private TableController tableController;

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

    public void setTableController(TableController tableController) {
        this.tableController = tableController;
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

    // ==================== REGULAR EXECUTION ====================


    public void handleStartRegular() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        // Stop debug session if it's running
        if (isDebugging) {
            handleStop();
            updateSummary.accept("Debug session stopped - starting regular execution");
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

    private void executeProgram(RunProgramDTO runDTO, List<Long> inputValues) {
        try {
            this.lastExecutionInputs = new ArrayList<>(inputValues);

            runDTO.setInput(inputValues);
            core.logic.execution.ResultCycle result = runDTO.runProgram();

            updateUIAfterExecution(runDTO, result);
            updateSummary.accept("Program executed successfully - Result: " + result.getResult() +
                    ", Cycles: " + result.getCycles());

        } catch (Exception e) {
            handleExecutionFailure(e);
        }
    }

    private void updateUIAfterExecution(RunProgramDTO runDTO, core.logic.execution.ResultCycle result) {
        executionResult.setCompleted(true);
        executionResult.setRunning(false);
        executionResult.setStatus("Completed");
        executionResult.setCycles((int) result.getCycles());

        updateVariablesWithResults(runDTO);

        executionResult.addToHistory("Result: " + result.getResult());
        executionResult.addToHistory("Execution Cycles: " + result.getCycles());
    }

    private void updateVariablesWithResults(RunProgramDTO runDTO) {
        variables.clear();

        Set<core.logic.variable.Variable> programVariables = runDTO.getOrderedVariables();
        List<Long> variableValues = runDTO.getOrderedValues();

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
    }

    // ==================== DEBUG EXECUTION ====================



    public void handleStartDebug() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            ExecuteProgramDTO executeDTO = engine.executeProgram();
            currentDebugSession = executeDTO.getDebugProgramDTO();

            Optional<List<Long>> inputValues = getDebugInputValues(currentDebugSession);
            if (inputValues.isEmpty()) {
                updateSummary.accept("Debug session cancelled by user");
                return;
            }

            lastExecutionInputs = new ArrayList<>(inputValues.get());
            currentDebugSession.setInput(inputValues.get());

            isDebugging = true;
            currentDebugIndex = 0;
            debugHistory.clear();

            executionResult.setRunning(true);
            executionResult.setStatus("Debugging");
            executionResult.setCompleted(false);

            updateVariablesWithDebugResults(currentDebugSession);
            highlightInstruction(currentDebugIndex);
            updateSummary.accept("Debug session started at instruction 0");

        } catch (Exception e) {
            showErrorDialog.accept("Debug Error", "Failed to start debug: " + e.getMessage());
            updateSummary.accept("Failed to start debug session");
        }
    }

    public void handleStop() {
        isDebugging = false;
        debugHistory.clear();
        currentDebugSession = null;
        clearInstructionHighlights();

        executionResult.setRunning(false);
        executionResult.setStatus("Stopped");
        updateSummary.accept("Debug session stopped");
    }

    public void handleResume() {
        if (!isDebugging || currentDebugSession == null) {
            showErrorDialog.accept("Resume Error", "No active debug session to resume.");
            return;
        }

        try {
            // Clear highlighting before resuming
            if (tableController != null) {
                tableController.clearHighlighting();
            }

            DebugFinalResult result = currentDebugSession.runUntilEnd();

        } catch (Exception e) {
            showErrorDialog.accept("Resume Error", "Failed to resume: " + e.getMessage());
            updateSummary.accept("Failed to resume execution");
        }
    }

    public void handleStepOver() {
        if (!isDebugging || currentDebugSession == null) {
            showErrorDialog.accept("Step Error", "No active debug session to step.");
            return;
        }

        try {
            DebugResult result = currentDebugSession.nextStep();

            if (result instanceof DebugFinalResult) {
                DebugFinalResult finalResult = (DebugFinalResult) result;
                currentDebugIndex = -1;
                clearInstructionHighlights();

                executionResult.setCompleted(true);
                executionResult.setRunning(false);
                executionResult.setStatus("Completed");
                executionResult.setCycles(finalResult.getCycles());

                updateVariablesWithDebugResults(currentDebugSession);

                updateSummary.accept("Program completed with result: " + finalResult.getResult() +
                        " (Total cycles: " + finalResult.getCycles() + ")");

                isDebugging = false;
                return;
            }

            debugHistory.add(result);
            currentDebugIndex = result.getNextIndex();
            executionResult.setCycles(result.getCycles());

            // IMPORTANT: Update ALL variables after each step to show current state
            updateVariablesWithDebugResults(currentDebugSession);

            highlightInstruction(currentDebugIndex);

            // REMOVED: Don't use the manual variable highlight during debug
            // The red instruction highlight is enough to show what's executing
            // If you want to show which variable changed, display it in the summary instead
            if (result.getChangedVariable() != null) {
                ChangedVariable changed = result.getChangedVariable();
                updateSummary.accept("Stepped to instruction " + currentDebugIndex +
                        " (Cycles: " + result.getCycles() +
                        ") - Changed: " + changed.getVariable().getRepresentation() +
                        " = " + changed.getNewValue());
            } else {
                updateSummary.accept("Stepped to instruction " + currentDebugIndex +
                        " (Cycles: " + result.getCycles() + ")");
            }

        } catch (Exception e) {
            showErrorDialog.accept("Step Error", "Failed to step: " + e.getMessage());
            updateSummary.accept("Failed to step over instruction");
        }
    }

    public void handleStepBack() {
        if (!isDebugging || debugHistory.isEmpty()) {
            showErrorDialog.accept("Cannot Step Back", "No previous steps to return to.");
            return;
        }

        try {
            debugHistory.remove(debugHistory.size() - 1);

            if (!debugHistory.isEmpty()) {
                ExecuteProgramDTO executeDTO = engine.executeProgram();
                currentDebugSession = executeDTO.getDebugProgramDTO();
                currentDebugSession.setInput(lastExecutionInputs);

                for (int i = 0; i < debugHistory.size(); i++) {
                    currentDebugSession.nextStep();
                }

                DebugResult lastResult = debugHistory.get(debugHistory.size() - 1);
                currentDebugIndex = lastResult.getNextIndex();

                // IMPORTANT: Update variables to reflect the state after stepping back
                updateVariablesWithDebugResults(currentDebugSession);

                highlightInstruction(currentDebugIndex);

                updateSummary.accept("Stepped back to instruction " + currentDebugIndex);
            } else {
                // Back to the beginning - reset to initial state
                ExecuteProgramDTO executeDTO = engine.executeProgram();
                currentDebugSession = executeDTO.getDebugProgramDTO();
                currentDebugSession.setInput(lastExecutionInputs);

                currentDebugIndex = 0;

                // Show initial variable values
                updateVariablesWithDebugResults(currentDebugSession);

                highlightInstruction(currentDebugIndex);
                updateSummary.accept("Stepped back to beginning - instruction 0");
            }

        } catch (Exception e) {
            showErrorDialog.accept("Step Back Error", "Failed to step back: " + e.getMessage());
            updateSummary.accept("Failed to step back: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private void highlightInstruction(int index) {
        currentDebugIndex = index;
        if (tableController != null) {
            tableController.highlightCurrentInstruction(index);
        }
    }

    private void clearInstructionHighlights() {
        if (tableController != null) {
            tableController.clearCurrentInstructionHighlight();
        }
    }

    private void updateChangedVariable(ChangedVariable changed) {
        // This method is no longer needed since we update all variables after each step
        // But we keep it to highlight the changed variable
        if (changed == null) return;

        if (tableController != null) {
            tableController.highlightVariable(changed.getVariable().getRepresentation());
        }
    }

    private void updateVariablesWithDebugResults(DebugProgramDTO debugDTO) {
        System.out.println("=== DEBUG: updateVariablesWithDebugResults ===");

        // First, let's verify the debug DTO is actually set up correctly
        System.out.println("DebugDTO input values: " + debugDTO.getOrderedInputValues());

        Set<core.logic.variable.Variable> programVariables = debugDTO.getOrderedVariables();
        List<Long> variableValues = debugDTO.getOrderedValues();

        System.out.println("Program variables count: " + (programVariables != null ? programVariables.size() : "null"));
        System.out.println("Variable values count: " + (variableValues != null ? variableValues.size() : "null"));

        // Print ALL values to see what we're getting
        if (variableValues != null) {
            System.out.println("ALL variable values: " + variableValues);
        }

        if (programVariables == null || variableValues == null) {
            System.err.println("ERROR: programVariables or variableValues is null!");
            return;
        }

        List<core.logic.variable.Variable> orderedVars = new ArrayList<>(programVariables);

        // Clear and rebuild
        variables.clear();

        for (int i = 0; i < Math.min(orderedVars.size(), variableValues.size()); i++) {
            core.logic.variable.Variable engineVar = orderedVars.get(i);
            Long value = variableValues.get(i);

            System.out.println("Creating UI Variable[" + i + "]: " +
                    engineVar.getRepresentation() + " = " + value +
                    " (Type: " + engineVar.getType() + ")");

            Variable uiVar = new Variable(
                    engineVar.getRepresentation(),
                    value.intValue(),
                    engineVar.getType().name()
            );

            variables.add(uiVar);
        }

        System.out.println("Variables list now has " + variables.size() + " items");
        System.out.println("=== END updateVariablesWithDebugResults ===");
    }

    private Optional<List<Long>> getInputValues(RunProgramDTO runDTO) {
        Set<core.logic.variable.Variable> requiredInputs = runDTO.getOrderedInputVariables();

        InputDialog inputDialog = new InputDialog(requiredInputs);
        inputDialog.initOwner(startRegularButton.getScene().getWindow());
        inputDialog.initModality(Modality.WINDOW_MODAL);

        return inputDialog.showAndWait();
    }

    private Optional<List<Long>> getDebugInputValues(DebugProgramDTO debugDTO) {
        Set<core.logic.variable.Variable> requiredInputs = debugDTO.getOrderedInputVariables();

        InputDialog inputDialog = new InputDialog(requiredInputs);
        inputDialog.initOwner(startRegularButton.getScene().getWindow());
        inputDialog.initModality(Modality.WINDOW_MODAL);

        return inputDialog.showAndWait();
    }

    private void handleExecutionFailure(Throwable exception) {
        showErrorDialog.accept("Execution Error", "Execution failed: " + exception.getMessage());
        executionResult.setRunning(false);
        executionResult.setStatus("Failed");
        updateSummary.accept("Execution failed: " + exception.getMessage());
    }

    // ==================== PROGRAM CONTROLS ====================

    public void handleRerun() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            ProgramStatisticsDTO statsDTO = engine.presentProgramStats();
            List<SingleRunStatisticDTO> programStats = statsDTO.getProgramStatisticCopy();

            if (programStats.isEmpty()) {
                showErrorDialog.accept("No Previous Runs", "No previous executions found.");
                return;
            }

            Optional<Integer> selectedRunNumber = showRunSelectionDialog(programStats);
            if (selectedRunNumber.isEmpty()) {
                updateSummary.accept("Rerun cancelled by user");
                return;
            }

            int runNumber = selectedRunNumber.get();
            ReExecuteProgramDTO reExecuteDTO = engine.reExecuteProgram(runNumber);

            PresentProgramDTO presentDTO = reExecuteDTO.getPresentProgramDTO();
            ExecuteProgramDTO executeDTO = reExecuteDTO.getExecuteProgramDTO();

            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(presentDTO));

            variables.clear();
            variables.addAll(ModelConverter.convertVariables(presentDTO));

            // Get the original inputs from the selected run
            SingleRunStatisticDTO selectedRun = programStats.stream()
                    .filter(stat -> stat.getRunNumber() == runNumber)
                    .findFirst()
                    .orElse(null);

            if (selectedRun == null) {
                showErrorDialog.accept("Rerun Error", "Could not find selected run statistics.");
                return;
            }

            List<Long> originalInputs = selectedRun.getInput();

            // Show input dialog with pre-filled values from the selected run
            RunProgramDTO runDTO = executeDTO.getRunProgramDTO();
            Set<core.logic.variable.Variable> requiredInputs = runDTO.getOrderedInputVariables();

            // Create dialog with pre-filled values
            InputDialog inputDialog = new InputDialog(requiredInputs, originalInputs);
            inputDialog.initOwner(startRegularButton.getScene().getWindow());
            inputDialog.initModality(Modality.WINDOW_MODAL);

            Optional<List<Long>> inputValues = inputDialog.showAndWait();
            if (inputValues.isEmpty()) {
                updateSummary.accept("Rerun cancelled by user");
                return;
            }

            // Execute with the (possibly modified) input values
            runDTO.setInput(inputValues.get());
            core.logic.execution.ResultCycle result = runDTO.runProgram();

            updateUIAfterExecution(runDTO, result);
            updateSummary.accept("Rerun of execution #" + runNumber + " completed - " +
                    "Result: " + result.getResult() + ", Cycles: " + result.getCycles());

        } catch (Exception e) {
            showErrorDialog.accept("Rerun Error", "Failed to rerun: " + e.getMessage());
        }
    }

    public void handleExpand() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            int maxDegree = currentProgram.getMaxDegree();
            if (currentDisplayDegree >= maxDegree) {
                updateSummary.accept("Already at maximum expansion degree (" + maxDegree + ")");
                return;
            }

            int newDegree = currentDisplayDegree + 1;
            PresentProgramDTO expandedProgram = engine.expandOrShrinkProgram(newDegree);

            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(expandedProgram));

            variables.clear();
            variables.addAll(ModelConverter.convertVariables(expandedProgram));

            currentDisplayDegree = newDegree;
            currentProgram.setCurrentDegree(newDegree);

            updateSummary.accept("Expanded to degree " + newDegree);

        } catch (Exception e) {
            showErrorDialog.accept("Expansion Error", "Failed to expand: " + e.getMessage());
        }
    }

    public void handleCollapse() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            if (currentDisplayDegree <= 0) {
                updateSummary.accept("Already at minimum degree (0)");
                return;
            }

            int newDegree = currentDisplayDegree - 1;
            PresentProgramDTO collapsedProgram = engine.expandOrShrinkProgram(newDegree);

            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(collapsedProgram));

            variables.clear();
            variables.addAll(ModelConverter.convertVariables(collapsedProgram));

            currentDisplayDegree = newDegree;
            currentProgram.setCurrentDegree(newDegree);

            updateSummary.accept("Collapsed to degree " + newDegree);

        } catch (Exception e) {
            showErrorDialog.accept("Collapse Error", "Failed to collapse: " + e.getMessage());
        }
    }

    public void handleShowStats() {
        try {
            ProgramStatisticsDTO statsDTO = engine.presentProgramStats();
            List<SingleRunStatisticDTO> programStats = statsDTO.getProgramStatisticCopy();

            statistics.clear();

            // Add individual run statistics FIRST
            for (SingleRunStatisticDTO runStat : programStats) {
                String inputStr = runStat.getInput().toString();

                statistics.add(new Statistic(
                        "Run #" + runStat.getRunNumber() + " (Degree " + runStat.getRunDegree() + ")",
                        (int) runStat.getCycles(),
                        runStat.getRunNumber(),
                        "Input: " + inputStr,
                        runStat.getResult()  // The result value (y)
                ));
            }

            // Calculate statistics
            int totalRuns = programStats.size();
            long totalCycles = 0;
            for (SingleRunStatisticDTO runStat : programStats) {
                totalCycles += runStat.getCycles();
            }

            // Count Basic vs Synthetic instructions
            int basicCount = 0;
            int syntheticCount = 0;
            for (Instruction inst : instructions) {
                if ("B".equals(inst.getType())) {
                    basicCount++;
                } else if ("S".equals(inst.getType())) {
                    syntheticCount++;
                }
            }

            // Add SUMMARY SECTION with separator rows
            statistics.add(new Statistic(
                    "═══ SUMMARY ═══",
                    0,
                    0,
                    "",
                    0
            ));

            statistics.add(new Statistic(
                    "Total Cycles",
                    (int) totalCycles,
                    0,
                    "Across " + totalRuns + " run(s)",
                    0
            ));

            statistics.add(new Statistic(
                    "Basic Commands",
                    basicCount,
                    0,
                    "Type: B",
                    0
            ));

            statistics.add(new Statistic(
                    "Synthetic Commands",
                    syntheticCount,
                    0,
                    "Type: S",
                    0
            ));

            updateSummary.accept(String.format("Statistics: %d runs | Total: %d cycles | Instructions: %d B, %d S",
                    totalRuns, totalCycles, basicCount, syntheticCount));

        } catch (Exception e) {
            showErrorDialog.accept("Statistics Error", "Failed to load statistics: " + e.getMessage());
        }
    }
    public void resetDisplayDegree() {
        this.currentDisplayDegree = 0;
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
        dialog.initOwner(startRegularButton.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);

        Optional<String> result = dialog.showAndWait();
        return result.map(selectedOption -> {
            int runIndex = runOptions.indexOf(selectedOption);
            return programStats.get(runIndex).getRunNumber();
        });
    }
}