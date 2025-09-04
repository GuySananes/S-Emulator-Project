package core.logic.instruction;

import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractInstructionTwoVariables extends AbstractInstruction {

    private final Variable secondaryVariable;

    public AbstractInstructionTwoVariables(InstructionData instructionData,
                                           Variable variable, Variable secondaryVariable) {
        super(instructionData, variable);
        this.secondaryVariable = secondaryVariable;
    }

    public AbstractInstructionTwoVariables(InstructionData instructionData,
                                           Variable variable, Variable secondaryVariable,
                                           Label label) {
        super(instructionData, variable, label);
        this.secondaryVariable = secondaryVariable;
    }

    public Variable getSecondaryVariable() {
        return secondaryVariable;
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> variables = super.getVariables();
        variables.add(secondaryVariable);
        return variables;
    }

    @Override
    public Set<Variable> getVariablesCopy() {
        Set<Variable> variables = super.getVariablesCopy();
        variables.add(secondaryVariable.copy());
        return variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractInstructionTwoVariables that = (AbstractInstructionTwoVariables) o;
        return Objects.equals(secondaryVariable, that.secondaryVariable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), secondaryVariable);
    }
}
