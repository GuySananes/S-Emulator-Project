package core.logic.instruction;

import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Set;

public abstract class AbstractInstructionTwoLabels extends AbstractInstruction {

    private final Label targetLabel;

    public AbstractInstructionTwoLabels(InstructionData instructionData,
                                        Label targetLabel) {
        super(instructionData);
        this.targetLabel = targetLabel;
    }

    public AbstractInstructionTwoLabels(InstructionData instructionData,
                                        Variable variable, Label targetLabel) {
        super(instructionData, variable);
        this.targetLabel = targetLabel;
    }

    public AbstractInstructionTwoLabels(InstructionData instructionData,
                                        Label label, Label targetLabel) {
        super(instructionData, label);
        this.targetLabel = targetLabel;
    }

    public AbstractInstructionTwoLabels(InstructionData instructionData,
                                        Variable variable, Label label,
                                        Label targetLabel) {
        super(instructionData, variable, label);
        this.targetLabel = targetLabel;
    }

    public Label getTargetLabel() {
        return targetLabel;
    }

    @Override
    public Set<Label> getLabels() {
        Set<Label> labels = super.getLabels();
        labels.add(targetLabel);
        return labels;
    }
}
