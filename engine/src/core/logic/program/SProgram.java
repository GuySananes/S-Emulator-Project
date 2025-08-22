package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;

import java.util.List;

public interface SProgram {

    String getName();
    void addInstruction(SInstruction instruction);
    List<SInstruction> getInstructionList();

    boolean validate();
    int calculateMaxDegree();
    int calculateCycles();

    SInstruction getInstructionAtIndex(int index);
    SInstruction getInstructionByLabel(Label label);

}