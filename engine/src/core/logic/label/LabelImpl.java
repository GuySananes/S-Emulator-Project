package core.logic.label;

import java.util.Objects;

public class LabelImpl implements Label {

    private final String label;
    private final int number;

    public LabelImpl(int number)
    {
        if (number <= 0) {
            throw new IllegalArgumentException("Label number must be positive.");
        }

        this.number = number;
        this.label = "L" + number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String getRepresentation() {return label;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelImpl label1 = (LabelImpl) o;
        return Objects.equals(label, label1.label);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(label);
    }
}