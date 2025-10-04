
package javafxUI.controller.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InputDialog extends Dialog<List<Long>> {

    private final VBox inputContainer;
    private final ObservableList<InputRow> inputRows;
    private final Set<core.logic.variable.Variable> requiredInputs;
    private final List<Long> prefilledValues; // Add this field

    public InputDialog(Set<core.logic.variable.Variable> requiredInputs) {
        this(requiredInputs, null); // Delegate to new constructor
    }

    // Add new constructor with prefilled values
    public InputDialog(Set<core.logic.variable.Variable> requiredInputs, List<Long> prefilledValues) {
        this.requiredInputs = requiredInputs;
        this.prefilledValues = prefilledValues;
        this.inputRows = FXCollections.observableArrayList();
        this.inputContainer = new VBox(10);

        setupDialog();
        createInitialInputs();
    }

    private void setupDialog() {
        setTitle("Program Input Values");
        setHeaderText("Enter input values for program execution");

        // Create dialog pane
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Main content
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));

        // Required variables info
        if (requiredInputs != null && !requiredInputs.isEmpty()) {
            Label infoLabel = new Label("Required input variables: " + requiredInputs);
            infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E7D32;");
            mainContent.getChildren().add(infoLabel);
        }

        // Instructions
        Label instructionsLabel = new Label("Enter values for each input (press + to add more inputs):");
        instructionsLabel.setStyle("-fx-font-style: italic;");
        mainContent.getChildren().add(instructionsLabel);

        // Input container with scroll
        ScrollPane scrollPane = new ScrollPane(inputContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");
        mainContent.getChildren().add(scrollPane);

        // Add input button
        Button addInputButton = new Button("+ Add Input");
        addInputButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white;");
        addInputButton.setOnAction(e -> addInputRow());
        mainContent.getChildren().add(addInputButton);

        dialogPane.setContent(mainContent);

        // Set result converter
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return collectInputValues();
            }
            return null;
        });

        // Validation
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateInputs()) {
                event.consume(); // Prevent dialog from closing
            }
        });
    }

    private void createInitialInputs() {
        // Add initial input rows based on required inputs or prefilled values
        int initialCount;
        if (prefilledValues != null && !prefilledValues.isEmpty()) {
            initialCount = prefilledValues.size();
        } else if (requiredInputs != null && !requiredInputs.isEmpty()) {
            initialCount = requiredInputs.size();
        } else {
            initialCount = 1;
        }

        for (int i = 0; i < initialCount; i++) {
            String prefilledValue = null;
            if (prefilledValues != null && i < prefilledValues.size()) {
                prefilledValue = String.valueOf(prefilledValues.get(i));
            }
            addInputRow(prefilledValue);
        }
    }

    private void addInputRow() {
        addInputRow(null); // Delegate to new method
    }

    private void addInputRow(String prefilledValue) {
        InputRow row = new InputRow(inputRows.size() + 1, prefilledValue);
        inputRows.add(row);
        inputContainer.getChildren().add(row.getRowNode());
    }

    private void removeInputRow(InputRow row) {
        if (inputRows.size() > 1) { // Keep at least one input
            inputRows.remove(row);
            inputContainer.getChildren().remove(row.getRowNode());
            updateRowNumbers();
        }
    }

    private void updateRowNumbers() {
        for (int i = 0; i < inputRows.size(); i++) {
            inputRows.get(i).updateNumber(i + 1);
        }
    }

    private List<Long> collectInputValues() {
        List<Long> values = new ArrayList<>();
        for (InputRow row : inputRows) {
            String value = row.getValue().trim();
            if (!value.isEmpty()) {
                try {
                    values.add(Long.parseLong(value));
                } catch (NumberFormatException e) {
                    // Skip invalid values (should be caught by validation)
                }
            }
        }
        return values;
    }

    private boolean validateInputs() {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < inputRows.size(); i++) {
            String value = inputRows.get(i).getValue().trim();
            if (value.isEmpty()) {
                errors.add("Input " + (i + 1) + " is empty");
            } else {
                try {
                    Long.parseLong(value);
                } catch (NumberFormatException e) {
                    errors.add("Input " + (i + 1) + " is not a valid number: " + value);
                }
            }
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please fix the following errors:");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return false;
        }

        return true;
    }

    // Inner class for input row
    private class InputRow {
        private final HBox rowNode;
        private final Label numberLabel;
        private final TextField valueField;
        private final Button removeButton;

        public InputRow(int number) {
            this(number, null); // Delegate to new constructor
        }

        public InputRow(int number, String prefilledValue) {
            numberLabel = new Label("Input " + number + ":");
            numberLabel.setMinWidth(80);

            valueField = new TextField();
            valueField.setPromptText("Enter numeric value");
            valueField.setPrefWidth(200);

            // Pre-fill the value if provided
            if (prefilledValue != null) {
                valueField.setText(prefilledValue);
            }

            removeButton = new Button("−");
            removeButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");
            removeButton.setOnAction(e -> removeInputRow(this));
            removeButton.setMinWidth(30);

            rowNode = new HBox(10);
            rowNode.getChildren().addAll(numberLabel, valueField, removeButton);
            rowNode.setStyle("-fx-padding: 5; -fx-border-color: #cccccc; -fx-border-radius: 3; -fx-background-color: white;");
        }

        public HBox getRowNode() {
            return rowNode;
        }

        public String getValue() {
            return valueField.getText();
        }

        public void updateNumber(int number) {
            numberLabel.setText("Input " + number + ":");
        }
    }
}