package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class JumpZero extends AbstractInstructionTwoLabels {

    public JumpZero(Variable variable, Label targetLabel) {
        this(variable, FixedLabel.EMPTY, targetLabel);
    }

    public JumpZero(Variable variable, Label label, Label targetLabel) {
        super(InstructionData.JUMP_ZERO, variable, label, targetLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        if (variableValue == 0) {
            return getTargetLabel();
        }

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = 0 GOTO " + getTargetLabel().getRepresentation();
    }




}
