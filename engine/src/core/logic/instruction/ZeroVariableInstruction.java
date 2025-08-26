package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class ZeroVariableInstruction extends AbstractInstruction{

    public ZeroVariableInstruction(Variable variable) {
        super(InstructionData.ZERO_VARIABLE, variable);
    }

    public ZeroVariableInstruction(Variable variable, Label label) {
        super(InstructionData.ZERO_VARIABLE, variable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), 0);
        return FixedLabel.EMPTY;
    }

    @Override
    public String getRepresentation() {
        return getVariable().getRepresentation() + " <- 0";
    }

}
