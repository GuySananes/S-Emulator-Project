package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;


public class JumpNotZeroInstruction extends AbstractInstructionTwoLabels{

    public JumpNotZeroInstruction(Variable variable, Label targetLabel) {
        this(variable, FixedLabel.EMPTY, targetLabel);
    }

    public JumpNotZeroInstruction(Variable variable, Label label, Label targetLabel) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label, targetLabel);
    }

    @Override
    public LabelCycle execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable());

        if (variableValue != 0) {
            return new LabelCycle(getTargetLabel(), Integer.parseInt(getInstructionData().getCycleRepresentation()));
        }

        return new LabelCycle(FixedLabel.EMPTY, Integer.parseInt(getInstructionData().getCycleRepresentation()));

    }

    @Override
    public String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " != 0 GOTO " + getTargetLabel().getRepresentation();
    }

    @Override
    public SInstruction clone() {
        return new JumpNotZeroInstruction(getVariable(), getLabel(), getTargetLabel());
    }
}