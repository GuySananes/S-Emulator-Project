package core.logic.instruction.mostInstructions;

import execution.ExecutionContext;
import execution.LabelCycleChangedVariable;
import core.logic.instruction.InstructionData;
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
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                getInstructionData().getCycles(),
                null);
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