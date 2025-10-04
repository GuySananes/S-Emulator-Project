package javafxUI.controller;

import core.logic.engine.Engine;
import exception.DegreeOutOfRangeException;
import exception.NoProgramException;
import exception.ProgramNotExecutedYetException;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafxUI.controller.dialog.InputDialog;
import javafxUI.model.ui.*;
import javafxUI.service.ModelConverter;
import javafxUI.service.ProgramExecutionService;
import present.program.PresentProgramDTO;
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

    private final ProgramExecutionService executionService = new ProgramExecutionService();

    private List<Long> lastExecutionInputs = new ArrayList<>();
    // Add these fields to track execution state for rerun
    private int lastExecutionDegree = 0;
    private boolean wasLastExecutionExpanded = false;

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
            Engine engine = Engine.getInstance(); // Changed from EngineImpl.getInstance()
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
            // Store the input values for statistics AND for potential rerun
            this.lastExecutionInputs = new ArrayList<>(inputValues);
            this.lastExecutionDegree = currentExpansionDegree;
            this.wasLastExecutionExpanded = isProgramExpanded;

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

            // *** REMOVED: Don't record statistics here - they're already recorded by the engine ***
            // recordExecutionStatistics(runDTO, result);

            updateSummary.accept("Execution completed - Result: " + result.getResult() + ", Cycles: " + result.getCycles());
            executionResult.addToHistory("Result: " + result.getResult());
            executionResult.addToHistory("Execution Cycles: " + result.getCycles());

        } catch (ProgramNotExecutedYetException e) {
            showErrorDialog.accept("Execution Error", "Program not executed yet: " + e.getMessage());
        } catch (Exception e) {
            showErrorDialog.accept("Execution Error", "Error updating UI: " + e.getMessage());
        }
    }

    // *** ADD THIS NEW METHOD ***


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
        if (!currentProgram.isLoaded()) {
            showErrorDialog.accept("No Program", "Please load a program first.");
            return;
        }

        // Check if there was a previous execution
        if (lastExecutionInputs == null || lastExecutionInputs.isEmpty()) {
            showErrorDialog.accept("No Previous Run",
                    "No previous execution found. Please run the program first.");
            return;
        }

        try {
            Engine engine = Engine.getInstance();
            RunProgramDTO runDTO = engine.runProgram();

            // Restore the program state to the degree used in the last execution
            if (lastExecutionDegree > 0) {
                // Expand/shrink to the last execution degree
                PresentProgramDTO programPresent = engine.expandOrShrinkProgram(lastExecutionDegree);
                instructions.clear();
                instructions.addAll(ModelConverter.convertInstructions(programPresent));

                currentDisplayDegree = lastExecutionDegree;
                currentExpansionDegree = lastExecutionDegree;
                isProgramExpanded = true;
                currentProgram.setCurrentDegree(lastExecutionDegree);
            } else if (currentDisplayDegree != 0) {
                // Restore to original program (degree 0)
                PresentProgramDTO programPresent = engine.expandOrShrinkProgram(0);
                instructions.clear();
                instructions.addAll(ModelConverter.convertInstructions(programPresent));

                currentDisplayDegree = 0;
                currentExpansionDegree = 0;
                isProgramExpanded = false;
                currentProgram.setCurrentDegree(0);
            }

            // Show input dialog with pre-filled values from last execution
            Optional<List<Long>> inputValues = getInputValuesForRerun(runDTO, lastExecutionInputs);

            if (inputValues.isEmpty()) {
                updateSummary.accept("Rerun cancelled by user");
                return;
            }

            // Store the new inputs for potential future reruns
            lastExecutionInputs = new ArrayList<>(inputValues.get());
            lastExecutionDegree = currentExpansionDegree;
            wasLastExecutionExpanded = isProgramExpanded;

            // Execute with the selected inputs
            runDTO.setInputs(inputValues.get());
            executeProgram(runDTO, inputValues.get());

            updateSummary.accept("Rerun completed with " +
                    (lastExecutionDegree > 0 ? "degree " + lastExecutionDegree : "original program"));

        } catch (Exception e) {
            showErrorDialog.accept("Rerun Error", "Failed to rerun program: " + e.getMessage());
            updateSummary.accept("Rerun failed: " + e.getMessage());
        }
    }

    private Optional<List<Long>> getInputValuesForRerun(RunProgramDTO runDTO, List<Long> prefilledValues) {
        Set<core.logic.variable.Variable> requiredInputs = runDTO.getInputs();

        // Create input dialog with pre-filled values from previous run
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
            Engine engine = Engine.getInstance();

            // Get the current program's degree information directly from engine
            RunProgramDTO runDTO = engine.runProgram();
            int maxDegree = runDTO.getMaxDegree();

            if (maxDegree == 0) {
                showErrorDialog.accept("Cannot Expand", "The program cannot be expanded.");
                return;
            }

            // Check if we can expand further
            if (currentDisplayDegree >= maxDegree) {
                updateSummary.accept("Already at maximum expansion degree (" + maxDegree + ")");
                return;
            }

            // Expand by 1 using the engine's expandOrShrinkProgram method
            int newDegree = currentDisplayDegree + 1;
            PresentProgramDTO expandedProgram = engine.expandOrShrinkProgram(newDegree);

            // Update instructions table with new degree
            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(expandedProgram));

            // Update current degree tracking
            currentDisplayDegree = newDegree;
            currentProgram.setCurrentDegree(newDegree);
            currentExpansionDegree = newDegree;
            isProgramExpanded = true;

            updateSummary.accept("Expanded to degree " + newDegree);

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
                updateSummary.accept("Already at minimum degree (0)");
                return;
            }

            Engine engine = Engine.getInstance();

            // Collapse by 1 using the engine's expandOrShrinkProgram method
            int newDegree = currentDisplayDegree - 1;
            PresentProgramDTO collapsedProgram = engine.expandOrShrinkProgram(newDegree);

            // Update instructions table with new degree
            instructions.clear();
            instructions.addAll(ModelConverter.convertInstructions(collapsedProgram));

            // Update current degree tracking
            currentDisplayDegree = newDegree;
            currentProgram.setCurrentDegree(newDegree);
            currentExpansionDegree = newDegree;
            isProgramExpanded = newDegree > 0;

            if (newDegree == 0) {
                updateSummary.accept("Showing original program (degree 0)");
            } else {
                updateSummary.accept("Collapsed to degree " + newDegree);
            }

        } catch (Exception e) {
            showErrorDialog.accept("Collapse Error", "Failed to collapse program: " + e.getMessage());
            updateSummary.accept("Collapse failed: " + e.getMessage());
        }
    }




    public void handleHighlight() {
        updateSummary.accept("Instructions highlighted");
    }

    public void handleShowStats() {
        try {
            // Use the engine's statistics DTO
            Engine engine = Engine.getInstance(); // Changed from EngineImpl.getInstance()
            ProgramStatisticsDTO statsDTO = engine.presentProgramStats(); // Returns ProgramStatisticsDTO, not SingleRunStatisticImpl

            // Get the list of individual run statistics from the DTO
            List<SingleRunStatisticDTO> programStats = statsDTO.getProgramStatisticCopy(); // This returns List<SingleRunStatisticDTO>

            // Clear existing statistics and populate with new data
            statistics.clear();

            // Add individual run statistics first
            for (int i = 0; i < programStats.size(); i++) {
                SingleRunStatisticDTO runStat = programStats.get(i); // Changed to SingleRunStatisticDTO

                Statistic uiStatistic = new Statistic(
                        "Run #" + runStat.getRunNumber() + " (Degree: " + runStat.getRunDegree() + ")",
                        (int) runStat.getCycles(),
                        0, // Total instructions - you can calculate this if needed
                        formatExecutionDetails(runStat) // Need to update this method signature
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
        } catch (Exception e) {
            showErrorDialog.accept("Statistics Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    private void addSummaryStatistics(ObservableList<Statistic> statisticsList, List<SingleRunStatisticDTO> programStats) {
        if (programStats.isEmpty()) return;

        // Calculate summary statistics
        long totalCycles = programStats.stream().mapToLong(SingleRunStatisticDTO::getCycles).sum();
        double avgCycles = (double) totalCycles / programStats.size();
        long maxCycles = programStats.stream().mapToLong(SingleRunStatisticDTO::getCycles).max().orElse(0);
        long minCycles = programStats.stream().mapToLong(SingleRunStatisticDTO::getCycles).min().orElse(0);

        // Add separator
        statisticsList.add(new Statistic("=== SUMMARY ===", 0, 0, ""));

        // Add summary rows - use the 2-column format properly
        statisticsList.add(new Statistic("Total Runs", programStats.size(), 0, ""));
        statisticsList.add(new Statistic("Total Cycles", (int) totalCycles, 0, ""));
        statisticsList.add(new Statistic("Average Cycles", (int) Math.round(avgCycles), 0, String.format("%.1f per run", avgCycles)));
        statisticsList.add(new Statistic("Min Cycles", (int) minCycles, 0, "Best performance"));
        statisticsList.add(new Statistic("Max Cycles", (int) maxCycles, 0, "Worst performance"));
    }

    private String formatExecutionDetails(SingleRunStatisticDTO runStat) {
        StringBuilder details = new StringBuilder();
        details.append("Result: ").append(runStat.getResult());

        // Note: SingleRunStatisticDTO doesn't have getInputCopy() method like SingleRunStatistic
        // If you need input information, you might need to get it differently or add it to the DTO
        // For now, we'll use the representation which should contain input info
        details.append(", ").append(runStat.getRepresentation());

        return details.toString();
    }
}