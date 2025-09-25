package core.logic.instruction.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractInstructionTwoLabels extends AbstractInstruction {

    private Label targetLabel;

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

    public void setTargetLabel(Label targetLabel) {
        this.targetLabel = targetLabel;
    }

    public Label getTargetLabel() {
        return targetLabel;
    }

    public Label getTargetLabelDeepCopy() {
        return targetLabel.deepCopy();
    }

    @Override
    public Set<Label> getLabels() {
        Set<Label> labels = super.getLabels();
        labels.add(targetLabel);
        return labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractInstructionTwoLabels that = (AbstractInstructionTwoLabels) o;
        return Objects.equals(targetLabel, that.targetLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), targetLabel);
    }
}
