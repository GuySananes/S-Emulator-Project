package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.variable.Variable;
import core.logic.variable.VariableType;

import java.util.*;

public class SProgramImpl implements SProgram{

    private final String name;
    private final List<SInstruction> instructionList;
    public SProgramImpl(String name) {
        this.name = name;
        instructionList = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addInstruction(SInstruction instruction) {
        if(instruction == null){
            throw new IllegalArgumentException("Instruction cannot be null when adding to program");
        }
        instructionList.add(instruction);
    }

    @Override
    public List<SInstruction> getInstructionList() {
        return instructionList;
    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public int calculateMaxDegree() {
        // traverse all commands and find maximum degree
        return 0;
    }

    @Override
    public int calculateCycles() {
        // traverse all commands and calculate cycles
        return 0;
    }

    @Override
    public String getRepresentation() {
        return "";
    }

    @Override
    public Set<Variable> getOrderedVariables() {
        Set<Variable> variables = new TreeSet<>();
        for (SInstruction instruction : instructionList) {
            variables.addAll(instruction.getVariables());
        }

        return variables;
    }

    @Override
    public Set<Variable> getInputVariables() {
        Set<Variable> allVariables = getOrderedVariables();
        Set<Variable> outputVariables = new HashSet<>();
        for(Variable variable : allVariables) {
            if(variable.getType() == VariableType.INPUT){
                outputVariables.add(variable);
            }
        }

        return outputVariables;
    }

    @Override
    public Set<Variable> getOrderedVariablesCopy() {
        Set<Variable> variables = new TreeSet<>();
        for (SInstruction instruction : instructionList) {
            variables.addAll(instruction.getVariablesCopy());
        }

        return variables;
    }

    @Override
    public Set<Label> getOrderedLabels() {
        Set<Label> labels = new TreeSet<>();
        for (SInstruction instruction : instructionList) {
            labels.add(instruction.getLabel());
        }

        return labels;
    }

    @Override
    public SInstruction getInstructionByLabel(Label label) {
        for (SInstruction instruction : instructionList) {
            if (instruction.getLabel().equals(label)) {
                return instruction;
            }
        }

        throw new NoSuchElementException("No instruction found with label: " +
                label.getRepresentation());
    }











}