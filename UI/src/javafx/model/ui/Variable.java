package javafx.model.ui;

import javafx.beans.property.*;

public class Variable {
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty value = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty();

    // Constructors
    public Variable() {}

    public Variable(String name, int value, String type) {
        this.name.set(name);
        this.value.set(value);
        this.type.set(type);
    }

    // Property getters for TableView binding
    public StringProperty nameProperty() { return name; }
    public IntegerProperty valueProperty() { return value; }
    public StringProperty typeProperty() { return type; }

    // Value getters/setters
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public int getValue() { return value.get(); }
    public void setValue(int value) { this.value.set(value); }

    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
}

