package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.label.LabelComparator;
import core.logic.variable.Variable;
import core.logic.variable.VariableType;

import java.util.*;

public class SProgramImpl implements SProgram{

    private final String name;
    private final List<SInstruction> instructionList;
    private int runNumber = 0;
    private Set<Variable> orderedVariables = null;
    private Set<Variable> inputVariables = null;
    private Set<Label> orderedLabels = null;

    public SProgramImpl(String name) {
        this.name = name;
        instructionList = new ArrayList<>();
    }

    private Set<Variable> calculateOrderedVariables() {
        Set<Variable> variables = new TreeSet<>();
        for (SInstruction instruction : instructionList) {
            variables.addAll(instruction.getVariables());
        }

        return variables;
    }

    private Set<Variable> calculateOrderedInputVariables() {
        Set<Variable> orderedVariables = getOrderedVariables();
        Set<Variable> inputVariables = new TreeSet<>();
        for(Variable variable : orderedVariables) {
            if(variable.getType() == VariableType.INPUT){
                inputVariables.add(variable);
            }
        }

        return inputVariables;
    }

    private Set<Label> calculateOrderedLabels() {
        Set<Label> labels = new TreeSet<>(new LabelComparator());
        for (SInstruction instruction : instructionList) {
            labels.addAll(instruction.getLabels());
        }

        return labels;
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
    public int getRunNumber() {
        return runNumber;
    }

    @Override
    public void incrementRunNumber() {
        runNumber++;
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
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < instructionList.size(); i++) {
            SInstruction instruction = instructionList.get(i);
            sb.append("#")
                    .append(i + 1)
                    .append(" ")
                    .append(instruction.getRepresentation())
                    .append(System.lineSeparator());
        }

        return sb.toString();
    }




    @Override
    public Set<Variable> getOrderedVariables() {
        if(orderedVariables == null){
            orderedVariables = calculateOrderedVariables();
        }

        return orderedVariables;
    }

    @Override
    public Set<Variable> getOrderedVariablesCopy() {
        Set<Variable> orderedVariables = getOrderedVariables();
        Set<Variable> copy = new TreeSet<>();
        for (Variable variable : orderedVariables) {
            copy.add(variable.copy());
        }

        return copy;
    }

    @Override
    public Set<Variable> getInputVariables() {
        if(inputVariables == null){
            inputVariables = calculateOrderedInputVariables();
        }

        return inputVariables;
    }

    @Override
    public Set<Variable> getInputVariablesCopy(){
        Set<Variable> inputVariables = getInputVariables();
        Set<Variable> copy = new TreeSet<>();
        for (Variable variable : inputVariables) {
            copy.add(variable.copy());
        }

        return copy;
    }

    @Override
    public Set<Label> getOrderedLabels() {
        if(orderedLabels == null){
            orderedLabels = calculateOrderedLabels();
        }

        return orderedLabels;
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