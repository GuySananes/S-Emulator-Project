package core.logic.program;

import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SProgram {

    String getName();
    SProgram getOriginalProgram();
    void addInstruction(SInstruction instruction);
    void addInstructions(List<SInstruction> instructions);
    List<SInstruction> getInstructionList();
    Set<Variable> getOrderedVariables();
    Set<Variable> getOrderedVariablesDeepCopy();
    Set<Label> getOrderedLabels();
    Set<Label> getOrderedLabelsDeepCopy();
    Set<Variable> getOrderedInputVariables();
    Set<Variable> getOrderedInputVariablesDeepCopy();
    int getDegree();
    SProgram clone();
    String getRepresentation();
    SInstruction getInstructionByLabel(Label Label);
    int getMinDegree();
    int getNumOfStaticInstructions();
    int getNumOfBasicInstructions();

    void addFunction(SFunction function);
    SFunction getFunction(String name);
    Map<String, SFunction> getFunctions();

}