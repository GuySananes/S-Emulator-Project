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
    int getRunNumber();
    void incrementRunNumber();
    Set<Variable> getOrderedVariables();
    Set<Variable> getOrderedVariablesCopy();
    Set<Label> getOrderedLabels();
    Set<Variable> getInputVariables();
    Set<Variable> getInputVariablesCopy();


    boolean validate();
    int calculateMaxDegree();
    int calculateCycles();
    String getRepresentation();
    SInstruction getInstructionByLabel(Label Label);
}