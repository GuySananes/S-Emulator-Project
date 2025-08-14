package core.logic.program;

import core.logic.instruction.SInstruction;

import java.util.List;

public interface SProgram {

    String getName();
    void addInstruction(SInstruction instruction);
    List<SInstruction> getInstructionList();

    boolean validate();
    int calculateMaxDegree();
    int calculateCycles();

}