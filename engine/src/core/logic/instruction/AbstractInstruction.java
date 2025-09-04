package core.logic.instruction;

import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractInstruction implements SInstruction {

    private final InstructionData instructionData;
    private final Label label;
    private final Variable variable;

    public AbstractInstruction(InstructionData instructionData) {
        this(instructionData, null, FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable) {
        this(instructionData, variable, FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, Label label) {
        this(instructionData, null, label);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable, Label label) {
        this.instructionData = instructionData;
        this.variable = variable;
        this.label = label != null ? label : FixedLabel.EMPTY;
    }



    @Override
    public String getName() {
        return instructionData.getName();
    }

    @Override
    public int getCycles() {
        return instructionData.getCycles();
    }

    @Override
    public int getDegree() {
        return instructionData.getDegree();
    }

    @Override
    public InstructionData getInstructionData() {
        return instructionData;
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public Variable getVariable() {
        return variable;
    }

    @Override
    public Variable getVariableCopy() {
        return variable != null ? variable.copy() : null;
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> variables = new HashSet<>();
        if (variable != null) {
            variables.add(variable);
        }

        return variables;
    }

    @Override
    public Set<Variable> getVariablesCopy() {
        Set<Variable> variables = new HashSet<>();
        if (variable != null) {
            variables.add(variable.copy());
        }

        return variables;
    }

    @Override
    public Set<Label> getLabels() {
        Set<Label> labels = new HashSet<>();
        if (label != FixedLabel.EMPTY) {
            labels.add(label);
        }

        return labels;
    }

    @Override
    public final String getRepresentation() {
        return "(" + instructionData.getInstructionType() + ") "
                + "[ " + label.getRepresentation() + " ] "
                + getCommandRepresentation()
                + " (" + instructionData.getCycles() + ")";
    }


    protected abstract String getCommandRepresentation();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractInstruction that = (AbstractInstruction) o;
        return instructionData == that.instructionData && Objects.equals(label, that.label) && Objects.equals(variable, that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instructionData, label, variable);
    }
}