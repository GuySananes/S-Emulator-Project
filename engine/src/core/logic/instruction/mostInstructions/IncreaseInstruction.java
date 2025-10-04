package core.logic.instruction.mostInstructions;

import core.logic.execution.ChangedVariable;
import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycleChangedVariable;
import core.logic.instruction.InstructionData;
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
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        Variable toChange = getVariable();
        long oldValue = context.getVariableValue(toChange);
        long newValue = oldValue + 1;
        context.updateVariable(toChange, newValue);
        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                getInstructionData().getCycles(),
                new ChangedVariable(toChange, oldValue, newValue));
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