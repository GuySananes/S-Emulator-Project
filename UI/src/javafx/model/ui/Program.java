package javafx.model.ui;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Program {
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty maxDegree = new SimpleIntegerProperty();
    private final IntegerProperty minDegree = new SimpleIntegerProperty();
    private final IntegerProperty totalCycles = new SimpleIntegerProperty();
    private final IntegerProperty totalInstructions = new SimpleIntegerProperty();
    private final ObservableList<Instruction> instructions = FXCollections.observableArrayList();
    private final ObservableList<Variable> variables = FXCollections.observableArrayList();
    private final StringProperty filePath = new SimpleStringProperty();
    private final BooleanProperty isLoaded = new SimpleBooleanProperty(false);

    // Constructors
    public Program() {}

    public Program(String name) {
        this.name.set(name);
    }

    // Property getters for JavaFX binding
    public StringProperty nameProperty() { return name; }
    public IntegerProperty maxDegreeProperty() { return maxDegree; }
    public IntegerProperty minDegreeProperty() { return minDegree; }
    public IntegerProperty totalCyclesProperty() { return totalCycles; }
    public IntegerProperty totalInstructionsProperty() { return totalInstructions; }
    public StringProperty filePathProperty() { return filePath; }
    public BooleanProperty isLoadedProperty() { return isLoaded; }

    // Observable lists for UI binding
    public ObservableList<Instruction> getInstructions() { return instructions; }
    public ObservableList<Variable> getVariables() { return variables; }

    // Value getters/setters
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public int getMaxDegree() { return maxDegree.get(); }
    public void setMaxDegree(int maxDegree) { this.maxDegree.set(maxDegree); }

    public int getMinDegree() { return minDegree.get(); }
    public void setMinDegree(int minDegree) { this.minDegree.set(minDegree); }

    public int getTotalCycles() { return totalCycles.get(); }
    public void setTotalCycles(int totalCycles) { this.totalCycles.set(totalCycles); }

    public int getTotalInstructions() { return totalInstructions.get(); }
    public void setTotalInstructions(int totalInstructions) { this.totalInstructions.set(totalInstructions); }

    public String getFilePath() { return filePath.get(); }
    public void setFilePath(String filePath) { this.filePath.set(filePath); }

    public boolean isLoaded() { return isLoaded.get(); }
    public void setLoaded(boolean loaded) { this.isLoaded.set(loaded); }

    // Utility methods
    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
        updateTotalInstructions();
    }

    public void addVariable(Variable variable) {
        variables.add(variable);
    }

    public void clearInstructions() {
        instructions.clear();
        updateTotalInstructions();
    }

    public void clearVariables() {
        variables.clear();
    }

    private void updateTotalInstructions() {
        setTotalInstructions(instructions.size());
    }

    @Override
    public String toString() {
        return getName() != null ? getName() : "Untitled Program";
    }
}
