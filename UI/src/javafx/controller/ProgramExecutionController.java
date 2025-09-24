
package javafx.controller;

import core.logic.engine.Engine;
import core.logic.engine.EngineImpl;
import exception.DegreeOutOfRangeException;
import exception.ProgramNotExecutedYetException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.controller.dialog.InputDialog;
import javafx.model.ui.*;
import javafx.scene.control.Button;
import javafx.service.ModelConverter;
import javafx.service.ProgramExecutionService;
import javafx.stage.Modality;
import present.PresentProgramDTO;
import run.RunProgramDTO;

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

    private final ProgramExecutionService executionService = new ProgramExecutionService();

    private int currentDisplayDegree = 0;

    public ProgramExecutionController(Program currentProgram,
                                      ExecutionResult executionResult,
                                      ObservableList<Instruction> instructions,
                                      ObservableList<Variable> variables,
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
        updateSummary.accept("Executing program with inputs: " + inputValues);
        executionResult.reset();
        executionResult.setRunning(true);
        executionResult.setStatus("Running");

        Task<Void> executionTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                core.logic.execution.ExecutionResult result = runDTO.runProgram();
                Platform.runLater(() -> updateUIAfterExecution(runDTO, result));
                return null;
            }
        };

        executionTask.setOnFailed(event -> handleExecutionFailure(executionTask.getException()));

        Thread executionThread = new Thread(executionTask);
        executionThread.setDaemon(true);
        executionThread.start();
    }

    private void updateUIAfterExecution(RunProgramDTO runDTO, core.logic.execution.ExecutionResult result) {
        try {
            executionResult.setCompleted(true);
            executionResult.setRunning(false);
            executionResult.setStatus("Completed");
            executionResult.setCycles(result.getCycles());

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
            Engine engine = EngineImpl.getInstance();
            expand.ExpandDTO expandDTO = engine.expandProgram();

            int minDegree = expandDTO.getMinDegree();

            // Check if we can collapse further
            if (currentDisplayDegree <= minDegree) {
                updateSummary.accept("Already at minimum degree (" + minDegree + ")");
                return;
            }

            // Collapse by 1
            int newDegree = currentDisplayDegree - 1;
            expandToDisplay(expandDTO, newDegree);

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
        updateSummary.accept("Statistics displayed");
    }
}