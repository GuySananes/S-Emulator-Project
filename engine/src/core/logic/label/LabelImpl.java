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

    public LabelImpl(String labelString) {
        if (labelString == null || labelString.trim().isEmpty()) {
            throw new IllegalArgumentException("Label string cannot be null or empty.");
        }

        // Normalize the label string to uppercase
        labelString = labelString.trim().toUpperCase();

        // Special case for EXIT label
        if ("EXIT".equals(labelString)) {
            this.label = "EXIT";
            this.number = -1; // Special number for EXIT label
            return;
        }

        // Check if the label starts with 'L'
        if (!labelString.startsWith("L")) {
            throw new IllegalArgumentException("Label must start with 'L' or 'l', got: " + labelString);
        }

        // Extract the number part
        String numberStr = labelString.substring(1);
        try {
            this.number = Integer.parseInt(numberStr);
            if (this.number <= 0) {
                throw new IllegalArgumentException("Label number must be positive, got: " + this.number);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid label number format: " + numberStr);
        }

        this.label = "L" + this.number;
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

    @Override
    public String toString() {
        return getRepresentation();
    }
}