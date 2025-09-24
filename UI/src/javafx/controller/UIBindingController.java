
package javafx.controller;

import javafx.model.ui.ExecutionResult;
import javafx.model.ui.Program;
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

    public UIBindingController(Program currentProgram,
                               ExecutionResult executionResult,
                               TextField loadedFilePath,
                               Label cyclesLabel,
                               TextArea historyChain) {
        this.currentProgram = currentProgram;
        this.executionResult = executionResult;
        this.loadedFilePath = loadedFilePath;
        this.cyclesLabel = cyclesLabel;
        this.historyChain = historyChain;
    }

    public void setupAllBindings() {
        setupProgramBindings();
        setupExecutionBindings();
    }

    private void setupProgramBindings() {
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

        // Bind execution history to history chain
        executionResult.getExecutionHistory().addListener((javafx.collections.ListChangeListener<String>) change -> {
            StringBuilder history = new StringBuilder();
            for (String step : executionResult.getExecutionHistory()) {
                history.append(step).append("\n");
            }
            historyChain.setText(history.toString());
        });
    }
}