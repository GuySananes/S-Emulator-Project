package javafxUI.model.ui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Instruction {
    private final IntegerProperty number = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty cycles = new SimpleStringProperty();  // Changed to StringProperty
    private final StringProperty description = new SimpleStringProperty();
    // New field for historical chain
    private final ObservableList<Instruction> historicalChain = FXCollections.observableArrayList();
    // Add to Instruction class:
    private boolean highlighted = false;


    // Constructors
    public Instruction() {}

    public Instruction(int number, String type, String cycles, String description) {  // Changed parameter type
        this.number.set(number);
        this.type.set(type);
        this.cycles.set(cycles);
        this.description.set(description);
    }

    // Property getters for TableView binding
    public IntegerProperty numberProperty() { return number; }
    public StringProperty typeProperty() { return type; }
    public StringProperty cyclesProperty() { return cycles; }  // Changed return type
    public StringProperty descriptionProperty() { return description; }

    // Value getters/setters
    public int getNumber() { return number.get(); }
    public void setNumber(int number) { this.number.set(number); }

    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }

    public String getCycles() { return cycles.get(); }  // Changed return type
    public void setCycles(String cycles) { this.cycles.set(cycles); }  // Changed parameter type

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    // Historical chain methods
    public ObservableList<Instruction> getHistoricalChain() {
        return historicalChain;
    }

    public void setHistoricalChain(ObservableList<Instruction> chain) {
        this.historicalChain.setAll(chain);
    }

    public void addToHistoricalChain(Instruction instruction) {
        this.historicalChain.add(instruction);
    }

    // Check if this instruction has a historical chain
    public boolean hasHistoricalChain() {
        return !historicalChain.isEmpty();
    }


    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;

    }

    public boolean isHighlighted() {
        return highlighted;
    }

}