package core.logic.variable;

import java.util.Objects;

public class VariableImpl implements Variable {
    private static final long DEFAULT_VALUE = 0;

    private final VariableType type;
    private final int number;
    private long value = DEFAULT_VALUE;

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
        return type.getVariableRepresentation(number);
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public void setValue(long value) {
        validateValue(value);
        this.value = value;
    }

    private void validateValue(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VariableImpl variable = (VariableImpl) obj;
        return number == variable.number && type == variable.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }
}