package core.logic.label;

public class LabelImpl implements Label{

    private final String label;

    public LabelImpl(int number) {
        label = "L" + number;
    }

    @Override
    public String getLabelRepresentation() {
        return label;
    }
}