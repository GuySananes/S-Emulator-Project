package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class IncreaseInstruction extends AbstractInstruction {

    public IncreaseInstruction(Variable variable) {
        super(InstructionData.INCREASE, variable);
    }

    public IncreaseInstruction(Variable variable, Label label) {
        super(InstructionData.INCREASE, variable, label);
    }

    @Override
    public LabelCycle execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getVariable());
        variableValue++;
        context.updateVariable(getVariable(), variableValue);

        return new LabelCycle(FixedLabel.EMPTY, Integer.parseInt(getInstructionData().getCycleRepresentation()));
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- " +
                getVariable().getRepresentation() + " + 1";
    }

    @Override
    public SInstruction clone() {
        return new IncreaseInstruction(getVariable(), getLabel());
    }
}