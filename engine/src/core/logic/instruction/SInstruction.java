package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Set;

/**
 * Represents a single instruction in the system.
 * Each instruction has a name, can be executed in a given context,
 * and may have associated cycles and labels.
 */
public interface SInstruction {

    String getName();
    Label execute(ExecutionContext context);
    int cycles();
    Label getLabel();
    Variable getVariable();
    Set<Variable> getXs();
    Set<Label> getLabels();
    String getInstructionRepresentation();
}