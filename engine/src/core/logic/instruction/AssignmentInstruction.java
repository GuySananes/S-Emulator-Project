package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Set;

public class AssignmentInstruction extends AbstractInstructionTwoVariables {

    public AssignmentInstruction(Variable variable, Variable secondaryVariable) {
        this(variable, secondaryVariable, FixedLabel.EMPTY);
    }

    public AssignmentInstruction(Variable variable, Variable secondaryVariable, Label label) {
        super(InstructionData.ASSIGNMENT, variable, secondaryVariable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long secondaryValue = context.getVariableValue(getSecondaryVariable());
        context.updateVariable(getVariable(), secondaryValue);
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- " + getSecondaryVariable().getRepresentation();
    }



}
