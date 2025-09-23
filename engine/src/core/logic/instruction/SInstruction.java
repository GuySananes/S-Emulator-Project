package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
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
    LabelCycle execute(ExecutionContext context);
    String getCycleRepresentation();
    Label getLabel();
    Variable getVariable();
    List<SInstruction> getParents();
    int getIndex();
    void setIndex(int index);
    void setParents(List<SInstruction> parents);
    void setVariable(Variable variable);
    void setLabel(Label label);
    Variable getVariableCopy();
    Set<Variable> getVariables();
    Set<Variable> getVariablesCopy();
    Set<Label> getLabels();
    String thisRepresentation();
    String getRepresentation();
    InstructionData getInstructionData();
    int getDegree();
    SInstruction clone();//currently without parents and index
}