package core.logic.instruction;

import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import core.logic.variable.VariableType;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractInstruction implements SInstruction {

    private final InstructionData instructionData;
    private final Label label;
    private final Variable variable;

    public AbstractInstruction(InstructionData instructionData, Variable variable) {
        this(instructionData, variable, FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable, Label label) {
        this.instructionData = instructionData;
        this.label = label;
        this.variable = variable;
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
    public Set<Variable> getXsCopy() {
        Set<Variable> Xs = new LinkedHashSet<>();
        if (variable != null && variable.getType() == VariableType.INPUT) {
            Xs.add(variable.copy());
        }

        return Xs;
    }

    @Override
    public Set<Label> getLabels() {
        Set<Label> labels = new LinkedHashSet<>();
        if (label != FixedLabel.EMPTY) {
            labels.add(label);
        }

        return labels;
    }
}