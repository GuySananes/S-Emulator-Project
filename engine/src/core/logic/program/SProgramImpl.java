package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.variable.Variable;

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
    public String getProgramRepresentation() {
        return "";
    }

    @Override
    public Set<Variable> getXs() {
        Set<Variable> Xs = new LinkedHashSet<>();
        for (SInstruction instruction : instructionList) {
            Xs.addAll(instruction.getXs());
        }

        return Xs;
    }

    @Override
    public Set<Label> getLabels() {
        Set<Label> labels = new LinkedHashSet<>();
        for (SInstruction instruction : instructionList) {
            labels.add(instruction.getLabel());
        }

        return labels;
    }





}