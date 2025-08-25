package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;

import java.util.ArrayList;
import java.util.List;

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
        // traverse all commands and find the maximum degree
        return 0;
    }

    @Override
    public int calculateCycles() {
        // traverse all commands and calculate cycles
        return 0;
    }

    @Override
    public SInstruction getInstructionAtIndex(int index) {
        if (index < 0 || index >= instructionList.size()) {
            return null;
        }
        return instructionList.get(index);
    }

    @Override
    public SInstruction getInstructionByLabel(Label label) {
        // Find the first instruction with a matching label
        for (SInstruction instruction : instructionList) {
            if (instruction.getLabel().equals(label)) {
                return instruction;
            }
        }
        return null;
    }
}