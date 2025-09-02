package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Set;

public class IndexedInstruction implements SInstruction{
    private final int index;
    private final SInstruction instruction;

    public IndexedInstruction(int index, SInstruction instruction) {
        this.index = index;
        this.instruction = instruction;
    }

    public SInstruction getInstruction() {
        return instruction;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int getDegree() {
        return instruction.getDegree();
    }

    @Override
    public Label execute(ExecutionContext context) {
        return instruction.execute(context);
    }

    @Override
    public String getName() {
        return instruction.getName();
    }

    @Override
    public int getCycles() {
        return instruction.getCycles();
    }

    @Override
    public Label getLabel() {
        return instruction.getLabel();
    }

    @Override
    public Variable getVariable() {
        return instruction.getVariable();
    }

    @Override
    public Variable getVariableCopy() {
        return instruction.getVariableCopy();
    }

    @Override
    public Set<Variable> getVariables() {
        return instruction.getVariables();
    }

    @Override
    public Set<Variable> getVariablesCopy() {
        return instruction.getVariablesCopy();
    }

    @Override
    public Set<Label> getLabels() {
        return instruction.getLabels();
    }

    @Override
    public InstructionData getInstructionData() {
        return instruction.getInstructionData();
    }

    @Override
    public String getRepresentation() {
        return String.format("%d: %s", index, instruction.getRepresentation());
    }
}

