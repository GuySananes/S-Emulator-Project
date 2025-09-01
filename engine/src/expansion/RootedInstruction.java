package expansion;

import core.logic.execution.ExecutionContext;
import core.logic.instruction.InstructionData;
import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;
import java.util.Set;

public class RootedInstruction implements SInstruction {
    private final SInstruction instruction;
    private final SInstruction rootInstructions;

    public RootedInstruction(SInstruction instruction, SInstruction rootInstructions) {
        this.instruction = instruction;
        this.rootInstructions = rootInstructions;
    }

    public SInstruction getInstruction() {
        return instruction;
    }

    public SInstruction getRootInstructions() {
        return rootInstructions;
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
    public Label execute(ExecutionContext context) {
        return instruction.execute(context);
    }

    @Override
    public String getRepresentation() {
        return instruction.getRepresentation() + " >>> " + rootInstructions.getRepresentation();
    }

    @Override
    public InstructionData getInstructionData() {
        return instruction.getInstructionData();
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







}
