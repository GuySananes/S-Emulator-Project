package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

public class ConstantAssignmentInstruction extends AbstractInstruction {

    private final long constantValue;

    public ConstantAssignmentInstruction(long constantValue, Variable variable) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable);
        this.constantValue = constantValue;
    }

    public ConstantAssignmentInstruction(long constantValue, Variable variable, Label label) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable, label);
        this.constantValue = constantValue;
    }

    public ConstantAssignmentInstruction(Variable variable, Label label) {

        super(InstructionData.CONSTANT_ASSIGNMENT, variable, label);
        this.constantValue = 0;
    }

    public ConstantAssignmentInstruction(Variable variable) {
        this(variable, FixedLabel.EMPTY);
    }


    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), constantValue);
        return FixedLabel.EMPTY;
    }

    @Override
    public String getRepresentation() {
        return getVariable().getRepresentation() + " <- " + constantValue;
    }
}
