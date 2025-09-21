package core.logic.program;

import core.logic.instruction.SInstruction;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;
import java.util.Set;

public interface SProgram {

    String getName();
    void addInstruction(SInstruction instruction);
    void addInstructions(List<SInstruction> instructions);
    List<SInstruction> getInstructionList();
    Set<Variable> getOrderedVariables();
    Set<Variable> getOrderedVariablesCopy();
    Set<Label> getOrderedLabels();
    Set<Variable> getInputVariables();
    Set<Variable> getInputVariablesCopy();
    int getDegree();
    int getCycles();
    String getRepresentation();
    SInstruction getInstructionByLabel(Label Label);
    int getMinDegree();

}