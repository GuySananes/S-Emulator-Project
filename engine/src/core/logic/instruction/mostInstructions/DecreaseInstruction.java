package core.logic.instruction.mostInstructions;

import execution.ChangedVariable;
import execution.ExecutionContext;
import execution.LabelCycleChangedVariable;
import core.logic.instruction.InstructionData;
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
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        Variable toChange = getVariable();
        long oldValue = context.getVariableValue(toChange);
        long newValue = Math.max(0, oldValue - 1);
        context.updateVariable(toChange, newValue);

        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                Integer.parseInt(getInstructionData().getCycleRepresentation()),
                newValue == oldValue ? null :
                        new ChangedVariable(toChange, oldValue, newValue));
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