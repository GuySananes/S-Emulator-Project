
package javafxUI.controller;

import core.logic.engine.Engine;
import core.logic.engine.EngineImpl;
import exception.DegreeOutOfRangeException;
import exception.NoProgramException;
import exception.ProgramHasNoStatisticException;
import exception.ProgramNotExecutedYetException;
import javafx.collections.ObservableList;
import javafxUI.controller.dialog.InputDialog;
import javafxUI.model.ui.*;
import javafx.scene.control.Button;
import javafxUI.model.ui.*;
import javafxUI.service.ModelConverter;
import javafxUI.service.ProgramExecutionService;
import javafx.stage.Modality;
import present.program.PresentProgramDTO;
import run.RunProgramDTO;
import statistic.ProgramStatisticDTO;
import statistic.SingleRunStatistic;

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

    private final ProgramExecutionService executionService = new ProgramExecutionService();

    private int currentDisplayDegree = 0;
    private int currentExpansionDegree = 0;
    private boolean isProgramExpanded = false;

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
            Engine engine = EngineImpl.getInstance();
            RunProgramDTO runDTO = engine.runProgram();

            setupExecutionDegree(runDTO);
            Optional<List<Long>> inputValues = getInputValues(runDTO);

            if (inputValues.isEmpty()) {
                updateSummary.accept("Execution cancelled by user");
                return;
            }

            runDTO.setInputs(inputValues.get());
            executeProgram(runDTO, inputValues.get());

        } catch (Exception e) {
            showErrorDialog.accept("Execution Error", "Failed to start execution: " + e.getMessage());
            updateSummary.accept("Failed to start execution: " + e.getMessage());
        }
    }

    private void setupExecutionDegree(RunProgramDTO runDTO) {
        try {
            if (runDTO.getMaxDegree() == 0) {
                updateSummary.accept("Program cannot be expanded, will be executed as is.");
                runDTO.setDegree(0);
            } else {
                runDTO.setDegree(0);
                updateSummary.accept("Executing with degree 0 (original program)");
            }
        } catch (DegreeOutOfRangeException e) {
            // This shouldn't happen when setting degree to 0, but handle it just in case
            showErrorDialog.accept("Degree Error", "Error setting execution degree: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Optional<List<Long>> getInputValues(RunProgramDTO runDTO) {
        Set<core.logic.variable.Variable> requiredInputs = runDTO.getInputs();

        InputDialog inputDialog = new InputDialog(requiredInputs);
        inputDialog.initOwner(startRegularButton.getScene().getWindow());
        inputDialog.initModality(Modality.WINDOW_MODAL);

        return inputDialog.showAndWait();
    }

    private void executeProgram(RunProgramDTO runDTO, List<Long> inputValues) {
        try {
            // If program is expanded, set the expansion degree
            if (isProgramExpanded && currentExpansionDegree > 0) {
                runDTO.setDegree(currentExpansionDegree);
            }

            runDTO.setInputs(inputValues);
            core.logic.execution.ResultCycle result = runDTO.runProgram();

            updateUIAfterExecution(runDTO, result);
            updateSummary.accept("Program executed successfully - " + result.getCycles() + " cycles" +
                    (isProgramExpanded ? " (expanded degree " + currentExpansionDegree + ")" : ""));

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

            // Get program representation (executed program, potentially expanded)
            PresentProgramDTO programPresent = runDTO.getPresentProgramDTO();
            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(programPresent));

            updateVariablesWithResults(runDTO);

            updateSummary.accept("Execution completed - Result: " + result.getResult() + ", Cycles: " + result.getCycles());
            executionResult.addToHistory("Result: " + result.getResult());
            executionResult.addToHistory("Execution Cycles: " + result.getCycles());

        } catch (ProgramNotExecutedYetException e) {
            showErrorDialog.accept("Execution Error", "Program not executed yet: " + e.getMessage());
        } catch (Exception e) {
            showErrorDialog.accept("Execution Error", "Error updating UI: " + e.getMessage());
        }
    }

    private void updateVariablesWithResults(RunProgramDTO runDTO) {
        try {
            variables.clear();

            Set<core.logic.variable.Variable> programVariables = runDTO.getOrderedVariablesCopy();
            List<Long> variableValues = runDTO.getOrderedValuesCopy();

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

            executionResult.addToHistory("Variables updated with execution results");

        } catch (ProgramNotExecutedYetException e) {
            showErrorDialog.accept("Variable Update Error", "Cannot get variable values: " + e.getMessage());
        }
    }

    private void handleExecutionFailure(Throwable exception) {
        showErrorDialog.accept("Execution Error", "Execution failed: " + exception.getMessage());
        executionResult.setRunning(false);
        executionResult.setStatus("Failed");
        updateSummary.accept("Execution failed: " + exception.getMessage());
    }

    // Debug methods (simplified for now)
    public void handleStartDebug() {
        updateSummary.accept("Debug mode not fully implemented yet");
    }

    public void handleStop() {
        executionService.stopExecution();
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
        if (currentProgram.isLoaded()) {
            handleStartRegular();
            updateSummary.accept("Rerunning last execution");
        } else {
            showErrorDialog.accept("No Program", "Please load a program first.");
        }
    }


    public void handleExpand() {
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        try {
            Engine engine = EngineImpl.getInstance();
            expand.ExpandDTO expandDTO = engine.expandProgram();

            int maxDegree = expandDTO.getMaxDegree();

            if (maxDegree == 0) {
                showErrorDialog.accept("Cannot Expand", "The program cannot be expanded.");
                return;
            }

            // Check if we can expand further
            if (currentDisplayDegree >= maxDegree) {
                updateSummary.accept("Already at maximum expansion degree (" + maxDegree + ")");
                return;
            }

            // Expand by 1
            int newDegree = currentDisplayDegree + 1;
            expandToDisplay(expandDTO, newDegree);

            // Track the expansion state
            currentExpansionDegree = newDegree;
            isProgramExpanded = true;

        } catch (Exception e) {
            showErrorDialog.accept("Expansion Error", "Failed to expand program: " + e.getMessage());
            updateSummary.accept("Expansion failed: " + e.getMessage());
        }
    }

    // Update handleCollapse to track state
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

            Engine engine = EngineImpl.getInstance();
            expand.ExpandDTO expandDTO = engine.expandProgram();

            int newDegree = currentDisplayDegree - 1;
            expandToDisplay(expandDTO, newDegree);

            // Track the expansion state
            currentExpansionDegree = newDegree;
            isProgramExpanded = newDegree > 0;

        } catch (Exception e) {
            showErrorDialog.accept("Collapse Error", "Failed to collapse program: " + e.getMessage());
            updateSummary.accept("Collapse failed: " + e.getMessage());
        }
    }


    /**
     * Helper method to expand/collapse to a specific degree and update UI
     */
    private void expandToDisplay(expand.ExpandDTO expandDTO, int targetDegree) {
        try {
            // Expand to the target degree
            PresentProgramDTO expandedProgram = expandDTO.expand(targetDegree);

            // Update instructions table with new degree
            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(expandedProgram));

            // Update current degree tracking
            currentDisplayDegree = targetDegree;
            currentProgram.setCurrentDegree(targetDegree);

            // Update summary
            if (targetDegree == 0) {
                updateSummary.accept("Showing original program (degree 0)");
            } else {
                updateSummary.accept("Expanded to degree " + targetDegree);
            }

        } catch (Exception e) {
            showErrorDialog.accept("Display Error", "Failed to display program at degree " + targetDegree + ": " + e.getMessage());
        }
    }

    public void handleHighlight() {
        updateSummary.accept("Instructions highlighted");
    }

    public void handleShowStats() {
        try {
            // Use the engine's statistics DTO
            Engine engine = EngineImpl.getInstance();
            ProgramStatisticDTO statsDTO = engine.presentProgramStats();

            // Get the list of individual run statistics from the DTO
            List<SingleRunStatistic> programStats = statsDTO.getProgramStatisticCopy();

            // Clear existing statistics and populate with new data
            statistics.clear();

            // Add individual run statistics first
            for (int i = 0; i < programStats.size(); i++) {
                SingleRunStatistic runStat = programStats.get(i);

                Statistic uiStatistic = new Statistic(
                        "Run #" + runStat.getRunNumber() + " (Degree: " + runStat.getRunDegree() + ")",
                        (int) runStat.getCycles(),
                        0, // Total instructions - you can calculate this if needed
                        formatExecutionDetails(runStat)
                );

                statistics.add(uiStatistic);
            }

            // Add summary statistics in a cleaner way
            addSummaryStatistics(statistics, programStats);

            updateSummary.accept("Statistics displayed - " + programStats.size() + " program runs shown.");

        } catch (NoProgramException e) {
            showErrorDialog.accept("No Program", "Please load a program first.");
        } catch (ProgramNotExecutedYetException e) {
            showErrorDialog.accept("No Statistics", "Please run the program first to see statistics.");
        } catch (ProgramHasNoStatisticException e) {
            updateSummary.accept("No statistics available for this program.");
        } catch (Exception e) {
            showErrorDialog.accept("Statistics Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    private void addSummaryStatistics(ObservableList<Statistic> statisticsList, List<SingleRunStatistic> programStats) {
        if (programStats.isEmpty()) return;

        // Calculate summary statistics
        long totalCycles = programStats.stream().mapToLong(SingleRunStatistic::getCycles).sum();
        double avgCycles = (double) totalCycles / programStats.size();
        long maxCycles = programStats.stream().mapToLong(SingleRunStatistic::getCycles).max().orElse(0);
        long minCycles = programStats.stream().mapToLong(SingleRunStatistic::getCycles).min().orElse(0);

        // Add separator
        statisticsList.add(new Statistic("=== SUMMARY ===", 0, 0, ""));

        // Add summary rows - use the 2-column format properly
        statisticsList.add(new Statistic("Total Runs", programStats.size(), 0, ""));
        statisticsList.add(new Statistic("Total Cycles", (int) totalCycles, 0, ""));
        statisticsList.add(new Statistic("Average Cycles", (int) Math.round(avgCycles), 0, String.format("%.1f per run", avgCycles)));
        statisticsList.add(new Statistic("Min Cycles", (int) minCycles, 0, "Best performance"));
        statisticsList.add(new Statistic("Max Cycles", (int) maxCycles, 0, "Worst performance"));
    }

    private String formatExecutionDetails(SingleRunStatistic runStat) {
        StringBuilder details = new StringBuilder();
        details.append("Result: ").append(runStat.getResult());

        List<Long> inputs = runStat.getInputCopy();
        if (!inputs.isEmpty()) {
            details.append(", Inputs: ").append(inputs.toString());
        }

        return details.toString();
    }
}