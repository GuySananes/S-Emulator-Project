package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Set;

public class JumpEqualVariable extends AbstractInstructionTwoVariables {

    private final Label targetLabel;

    public JumpEqualVariable(Variable variable, Variable secondaryVariable, Label targetLabel) {
        this(variable, secondaryVariable, targetLabel, FixedLabel.EMPTY);
    }

    public JumpEqualVariable(Variable variable, Variable secondaryVariable, Label targetLabel, Label label) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, variable, secondaryVariable, label);
        this.targetLabel = targetLabel;
    }

    public JumpEqualVariable(Variable variable, Label label) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, variable, variable, label);
        this.targetLabel = label;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        long secondaryValue = context.getVariableValue(getSecondaryVariable());
        if (variableValue == secondaryValue) {
            return targetLabel;
        }

        return FixedLabel.EMPTY;
    }

    @Override
    public String getRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = "
                + getSecondaryVariable().getRepresentation() +
                " GOTO " + targetLabel.getRepresentation();
    }

    @Override
    public Set<Label> getLabels() {
        Set<Label> labels = super.getLabels();
        labels.add(targetLabel);
        return labels;
    }
}
