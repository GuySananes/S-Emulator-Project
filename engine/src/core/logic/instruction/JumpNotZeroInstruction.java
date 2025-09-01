package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Set;

public class JumpNotZeroInstruction extends AbstractInstructionTwoLabels{

    public JumpNotZeroInstruction(Variable variable, Label targetLabel) {
        this(variable, targetLabel, FixedLabel.EMPTY);
    }

    public JumpNotZeroInstruction(Variable variable, Label label, Label targetLabel) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label, targetLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());

        if (variableValue != 0) {
            return getTargetLabel();
        }

        return FixedLabel.EMPTY;

    }

    @Override
    public String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " != 0 GOTO " + getTargetLabel().getRepresentation();
    }
}