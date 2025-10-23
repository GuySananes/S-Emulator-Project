package javafxUI.model.ui;

import javafx.beans.property.*;

public class SLabel {
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty position = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final BooleanProperty isFixed = new SimpleBooleanProperty(false);

    // Constructors
    public SLabel() {}

    public SLabel(String name, int position) {
        this.name.set(name);
        this.position.set(position);
    }

    public SLabel(String name, int position, String type) {
        this.name.set(name);
        this.position.set(position);
        this.type.set(type);
    }

    // Property getters for JavaFX binding
    public StringProperty nameProperty() { return name; }
    public IntegerProperty positionProperty() { return position; }
    public StringProperty typeProperty() { return type; }
    public BooleanProperty isFixedProperty() { return isFixed; }

    // Value getters/setters
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public int getPosition() { return position.get(); }
    public void setPosition(int position) { this.position.set(position); }

    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }

    public boolean isFixed() { return isFixed.get(); }
    public void setFixed(boolean fixed) { this.isFixed.set(fixed); }

    @Override
    public String toString() {
        return getName() != null ? getName() : "SLabel@" + getPosition();
    }
}
