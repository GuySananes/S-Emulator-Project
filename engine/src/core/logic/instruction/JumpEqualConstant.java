package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class JumpEqualConstant extends AbstractInstructionTwoLabels{

    private final long constantValue;

    public JumpEqualConstant(Variable variable, long constantValue, Label targetLabel) {
        this(variable, constantValue, FixedLabel.EMPTY, targetLabel);
    }

    public JumpEqualConstant(Variable variable, long constantValue, Label label, Label targetLabel) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, variable, label, targetLabel);
        this.constantValue = constantValue;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        if (variableValue == constantValue) {
            return getTargetLabel();
        }

        return FixedLabel.EMPTY;
    }

    @Override
    public String getRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = " + constantValue + " GOTO " + getTargetLabel().getRepresentation();
    }
}
