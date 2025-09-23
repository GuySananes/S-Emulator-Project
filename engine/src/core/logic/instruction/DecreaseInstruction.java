package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class DecreaseInstruction extends AbstractInstruction {

    public DecreaseInstruction(Variable variable) {
        super(InstructionData.DECREASE, variable);
    }

    public DecreaseInstruction(Variable variable, Label label) {
        super(InstructionData.DECREASE, variable, label);
    }

    @Override
    public LabelCycle execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable());
        variableValue = Math.max(0, variableValue - 1);
        context.updateVariable(getVariable(), variableValue);

        return new LabelCycle(FixedLabel.EMPTY, Integer.parseInt(getInstructionData().getCycleRepresentation()));
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- " +
                getVariable().getRepresentation() + " - 1";
    }

    @Override
    public SInstruction clone() {
        return new DecreaseInstruction(getVariable(), getLabel());
    }

}