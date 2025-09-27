package core.logic.execution;

import core.logic.label.Label;

public class LabelCycle {
    private final Label label;
    private final int cycles;

    public LabelCycle(Label label, int cycles){
        this.label = label;
        this.cycles = cycles;
    }

    public Label getLabel() {
        return label;
    }

    public int getCycles() {
        return cycles;
    }
}
