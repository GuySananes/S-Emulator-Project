package core.logic.execution;

import core.logic.label.Label;

public class LabelCycleChangedVariable {
    private final Label label;
    private final int cycles;
    private final ChangedVariable changedVariable;


    public LabelCycleChangedVariable(Label label, int cycles, ChangedVariable changedVariable) {
        this.label = label;
        this.cycles = cycles;
        this.changedVariable = changedVariable;
    }

    public Label getLabel() {
        return label;
    }

    public int getCycles() {
        return cycles;
    }

    public ChangedVariable getChangedVariable() {
        return changedVariable;
    }
}
