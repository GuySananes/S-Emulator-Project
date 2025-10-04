package core.logic.instruction.mostInstructions;

import execution.ExecutionContext;
import execution.LabelCycleChangedVariable;
import core.logic.instruction.InstructionData;
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
    LabelCycleChangedVariable execute(ExecutionContext context);
    String getCycleRepresentation();
    Label getLabel();
    Label getLabelDeepCopy();
    Variable getVariable();
    List<SInstruction> getParents();
    int getIndex();
    Variable getVariableDeepCopy();
    Set<Variable> getVariables();
    Set<Label> getLabels();
    String getRepresentation();
    String getParentsRepresentation();
    InstructionData getInstructionData();
    int getDegree();

    void setIndex(int index);
    void setParents(List<SInstruction> parents);
    void setVariable(Variable variable);
    void setLabel(Label label);

    SInstruction clone();//currently without parents and index
    boolean isBasic();

}