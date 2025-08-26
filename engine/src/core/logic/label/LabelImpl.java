package core.logic.label;

import java.util.Objects;

public class LabelImpl implements Label, Comparable<Label> {

    private final String label;

    public LabelImpl(int number) {
        label = "L" + number;
    }

    @Override
    public String getRepresentation() {
        return label;
    }

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

    @Override
    public int compareTo(Label other) {
        if(other == FixedLabel.EXIT) {
            return 1;
        }

        return this.label.compareTo(other.getRepresentation());
    }
}