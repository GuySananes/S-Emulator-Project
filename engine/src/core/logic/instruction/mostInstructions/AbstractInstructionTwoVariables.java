package core.logic.instruction.mostInstructions;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Objects;
import java.util.Set;

public abstract class AbstractInstructionTwoVariables extends AbstractInstruction {

    private Variable secondVariable;

    public AbstractInstructionTwoVariables(InstructionData instructionData,
                                           Variable variable, Variable secondVariable) {
        super(instructionData, variable);
        this.secondVariable = secondVariable;
    }

    public AbstractInstructionTwoVariables(InstructionData instructionData,
                                           Variable variable, Variable secondVariable,
                                           Label label) {
        super(instructionData, variable, label);
        this.secondVariable = secondVariable;
    }

    public void setSecondVariable(Variable secondVariable) {
        this.secondVariable = secondVariable;
    }


    public Variable getSecondVariable() {
        return secondVariable;
    }

    public Variable getSecondVariableDeepCopy() {
        return secondVariable.deepCopy();
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> variables = super.getVariables();
        variables.add(secondVariable);
        return variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractInstructionTwoVariables that = (AbstractInstructionTwoVariables) o;
        return Objects.equals(secondVariable, that.secondVariable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), secondVariable);
    }
}
