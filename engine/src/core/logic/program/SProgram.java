package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;
import java.util.Set;

public interface SProgram {

    String getName();
    void addInstruction(SInstruction instruction);
    List<SInstruction> getInstructionList();
    Set<Variable> getXsCopy();
    Set<Label> getLabels();

    boolean validate();
    int calculateMaxDegree();
    int calculateCycles();
    String getRepresentation();

}