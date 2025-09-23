package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class NoOpInstruction extends AbstractInstruction {

    public NoOpInstruction(Variable variable) {
        super(InstructionData.NO_OP, variable);
    }

    public NoOpInstruction(Variable variable, Label label) {
        super(InstructionData.NO_OP, variable, label);
    }

    @Override
    public LabelCycle execute(ExecutionContext context) {
        return new LabelCycle(FixedLabel.EMPTY, Integer.parseInt(getInstructionData().getCycleRepresentation()));
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- " + getVariable().getRepresentation();
    }

    @Override
    public SInstruction clone() {
        return new NoOpInstruction(getVariable(), getLabel());
    }
}