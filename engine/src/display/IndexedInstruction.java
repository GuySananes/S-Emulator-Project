package display;

import core.logic.execution.ExecutionContext;
import core.logic.instruction.InstructionData;
import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Set;

public class IndexedInstruction implements SInstruction{
    private int index;
    private final SInstruction instruction;

    public IndexedInstruction(int index, SInstruction instruction) {
        this.index = index;
        this.instruction = instruction;
    }

    public int getIndex() {
        return index;
    }

    public SInstruction getInstruction() {
        return instruction;
    }

    public void setIndex(int index){
        if(index < 0){
            throw new IllegalArgumentException("Index cannot be negative");
        }

        this.index = index;
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
    public String getRepresentation() {
        // You can customize the representation to include the index
        return String.format("%d: %s", index, instruction.getRepresentation());
    }

    @Override
    public InstructionData getInstructionData() {
        return instruction.getInstructionData();
    }


}

