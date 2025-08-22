package core.logic.variable;

import java.util.Objects;

public class VariableImpl implements Variable {

    private final VariableType type;
    private final int number;

    public VariableImpl(VariableType type, int number) {
        this.type = type;
        this.number = number;
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public String getRepresentation() {
        return type.getRepresentation(number);
    }

    @Override
    public Variable copy() {
        return new VariableImpl(type, number);
    }

    @Override
    public String toString() {
        return getRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableImpl variable = (VariableImpl) o;
        return number == variable.number && type == variable.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }


}