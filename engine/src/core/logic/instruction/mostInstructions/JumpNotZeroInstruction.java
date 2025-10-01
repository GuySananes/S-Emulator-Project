package core.logic.instruction.mostInstructions;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycleChangedVariable;
import core.logic.instruction.InstructionData;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;


public class JumpNotZeroInstruction extends AbstractInstructionTwoLabels {

    public JumpNotZeroInstruction(Variable variable, Label targetLabel) {
        this(variable, FixedLabel.EMPTY, targetLabel);
    }

    public JumpNotZeroInstruction(Variable variable, Label label, Label targetLabel) {
        super(InstructionData.JUMP_NOT_ZERO, variable, label, targetLabel);
    }

    @Override
    public LabelCycleChangedVariable execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable());

        if (variableValue != 0) {
            return new LabelCycleChangedVariable(getTargetLabel(),
                    Integer.parseInt(getInstructionData().getCycleRepresentation()),
                    null);
        }

        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                Integer.parseInt(getInstructionData().getCycleRepresentation()),
                null);

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