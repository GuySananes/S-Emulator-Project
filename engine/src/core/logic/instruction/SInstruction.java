package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.List;
import java.util.Set;

/**
 * Represents a single instruction in the system.
 * Each instruction has a name, can be executed in a given context,
 * and may have associated cycles and labels.
 */
public interface SInstruction {

    String getName();
    Label execute(ExecutionContext context);
    int getCycles();
    Label getLabel();
    Variable getVariable();
    List<SInstruction> getParents();
    int getIndex();
    void setIndex(int index);
    void setParents(List<SInstruction> parents);
    Variable getVariableCopy();
    Set<Variable> getVariables();
    Set<Variable> getVariablesCopy();
    Set<Label> getLabels();
    String getRepresentation();
    InstructionData getInstructionData();
    int getDegree();
}