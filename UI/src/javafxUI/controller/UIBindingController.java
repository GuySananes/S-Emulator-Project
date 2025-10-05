package javafxUI.controller;

import javafxUI.model.ui.ExecutionResult;
import javafxUI.model.ui.Program;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Handles all UI data binding setup
 */
public class UIBindingController {

    private final Program currentProgram;
    private final ExecutionResult executionResult;
    private final TextField loadedFilePath;
    private final Label cyclesLabel;
    private final TextArea historyChain;
    private final Label currentDegreeLabel;

    public UIBindingController(Program currentProgram,
                               ExecutionResult executionResult,
                               TextField loadedFilePath,
                               Label cyclesLabel,
                               TextArea historyChain,
                               Label currentDegreeLabel) {
        this.currentProgram = currentProgram;
        this.executionResult = executionResult;
        this.loadedFilePath = loadedFilePath;
        this.cyclesLabel = cyclesLabel;
        this.historyChain = historyChain;
        this.currentDegreeLabel = currentDegreeLabel;
    }

    public void setupAllBindings() {
        // Bind file path
        if (loadedFilePath != null) {
            loadedFilePath.textProperty().bind(currentProgram.filePathProperty());
        }

        // Bind cycles
        if (cyclesLabel != null) {
            cyclesLabel.textProperty().bind(
                    javafx.beans.binding.Bindings.concat("Cycles: ", executionResult.cyclesProperty())
            );
        }

        // Bind history chain - convert ObservableList to String
        if (historyChain != null) {
            executionResult.getExecutionHistory().addListener((javafx.collections.ListChangeListener<String>) change -> {
                StringBuilder historyText = new StringBuilder();
                for (String historyItem : executionResult.getExecutionHistory()) {
                    historyText.append(historyItem).append("\n");
                }
                historyChain.setText(historyText.toString());
            });
        }

        // Bind current degree with program name
        if (currentDegreeLabel != null) {
            currentDegreeLabel.textProperty().bind(
                    javafx.beans.binding.Bindings.concat(
                            "Current Degree: ",
                            currentProgram.currentDegreeProperty(),
                            " / ",
                            currentProgram.maxDegreeProperty(),
                            " (Program: ",
                            currentProgram.nameProperty(),
                            ")"
                    )
            );
        }
    }

    private void setupProgramBindings() {
        // Bind the degree label to show "current / max"
        currentDegreeLabel.textProperty().bind(
                currentProgram.currentDegreeProperty().asString()
                        .concat(" / ")
                        .concat(currentProgram.maxDegreeProperty().asString())
        );

        // Bind program name to loaded file path (when no file is selected)
        loadedFilePath.textProperty().bind(
                javafx.beans.binding.Bindings.when(currentProgram.filePathProperty().isNotEmpty())
                        .then(currentProgram.filePathProperty())
                        .otherwise("No file loaded")
        );
    }

    private void setupExecutionBindings() {
        // Bind execution result to cycles label
        cyclesLabel.textProperty().bind(executionResult.cyclesProperty().asString());

        // Bind execution history to history chain - only if historyChain exists
        if (historyChain != null) {
            executionResult.getExecutionHistory().addListener((javafx.collections.ListChangeListener<String>) change -> {
                StringBuilder history = new StringBuilder();
                for (String step : executionResult.getExecutionHistory()) {
                    history.append(step).append("\n");
                }
                historyChain.setText(history.toString());
            });
        }
        // If historyChain is null (removed from FXML), the binding is simply skipped
        // and no history display will be updated, which is the intended behavior
    }
}