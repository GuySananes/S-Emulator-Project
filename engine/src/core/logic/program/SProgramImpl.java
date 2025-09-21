package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.label.LabelComparator;
import core.logic.variable.Variable;
import core.logic.variable.VariableType;

import java.util.*;

public class SProgramImpl implements SProgram{

    private int index = 1;
    private final String name;
    private final List<SInstruction> instructionList;
    private Set<Variable> orderedVariables = null;
    private Set<Variable> inputVariables = null;
    private Set<Label> orderedLabels = null;
    private int cycles = -1;
    private int degree = -1;
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

    private int calculateDegree() {
        int maxDegree = 0;
        for (SInstruction instruction : instructionList) {
            int degree = instruction.getDegree();
            if (degree > maxDegree) {
                maxDegree = degree;
            }
        }

        return maxDegree;
    }

    private int calculateCycles() {
        int totalCycles = 0;
        for (SInstruction instruction : instructionList) {
            totalCycles += instruction.getCycles();
        }

        return totalCycles;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCycles() {
        if(cycles == -1){
            cycles = calculateCycles();
        }

        return cycles;
    }

    @Override
    public int getDegree() {
        if(degree == -1){
            degree = calculateDegree();
        }

        return degree;
    }

    @Override
    public void addInstruction(SInstruction instruction) {
        if(instruction == null){
            throw new IllegalArgumentException("Instruction cannot be null when adding to program");
        }

        instruction.setIndex(index++);

        instructionList.add(instruction);
    }

    @Override
    public void addInstructions(List<SInstruction> instructions) {
        if(instructions == null){
            throw new IllegalArgumentException("Instructions List cannot be null when added to program");
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
        if (o == null || getClass() != o.getClass()) return false;
        SProgramImpl sProgram = (SProgramImpl) o;
        return index == sProgram.index && Objects.equals(name, sProgram.name) && Objects.equals(instructionList, sProgram.instructionList) && Objects.equals(orderedVariables, sProgram.orderedVariables) && Objects.equals(inputVariables, sProgram.inputVariables) && Objects.equals(orderedLabels, sProgram.orderedLabels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, name, instructionList, orderedVariables, inputVariables, orderedLabels);
    }
}