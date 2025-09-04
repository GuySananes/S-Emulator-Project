package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.label.LabelComparator;
import core.logic.variable.Variable;
import core.logic.variable.VariableType;
import core.logic.instruction.IndexedInstruction;

import java.util.*;

public class SProgramImpl implements SProgram{

    private int index = 1;
    private final String name;
    private final List<SInstruction> instructionList;
    private int runNumber = 0;
    private Set<Variable> orderedVariables = null;
    private Set<Variable> inputVariables = null;
    private Set<Label> orderedLabels = null;
    private static final int MIN_DEGREE = 0;

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

        SInstruction toAdd;

        if (instruction instanceof IndexedInstruction ii) {
            toAdd = new IndexedInstruction(index++, ii.getInstruction());
        } else {
            toAdd = new IndexedInstruction(index++, instruction);
        }

        instructionList.add(toAdd);
    }

    @Override
    public void addInstructions(List<SInstruction> instructions) {
        if(instructions == null){
            throw new IllegalArgumentException("Instructions List cannot be null when adding to program");
        }

        for(SInstruction instruction : instructions){
            addInstruction(instruction);
        }
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
    public int calculateMaxDegree() {
        //every instruction has a degree, return the max degree of all instructions
        int maxDegree = 0;
        for (SInstruction instruction : instructionList) {
            int degree = instruction.getDegree();
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }

        return maxDegree;
    }

    @Override
    public int calculateCycles() {
        int totalCycles = 0;
        for (SInstruction instruction : instructionList) {
            totalCycles += instruction.getCycles();
        }

        return totalCycles;
    }

    @Override
    public String getRepresentation() {
        StringBuilder sb = new StringBuilder();
        for (SInstruction instruction : instructionList) {
            sb.append(instruction.getRepresentation()).append(System.lineSeparator());
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
        // Special case: if the label is EXIT, the program should end
        if ("EXIT".equals(label.getRepresentation())) {
            return null; // Returning null indicates program termination
        }

        for (SInstruction instruction : instructionList) {
            if (instruction.getLabel().equals(label)) {
                return instruction;
            }
        }

        throw new NoSuchElementException("No instruction found with label: " +
                label.getRepresentation());
    }

    @Override
    public int getMinDegree() {
        return MIN_DEGREE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SProgramImpl sProgram = (SProgramImpl) o;
        return index == sProgram.index && runNumber == sProgram.runNumber && Objects.equals(name, sProgram.name) && Objects.equals(instructionList, sProgram.instructionList) && Objects.equals(orderedVariables, sProgram.orderedVariables) && Objects.equals(inputVariables, sProgram.inputVariables) && Objects.equals(orderedLabels, sProgram.orderedLabels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, name, instructionList, runNumber, orderedVariables, inputVariables, orderedLabels);
    }
}