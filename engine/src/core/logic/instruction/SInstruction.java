package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.Label;
import core.logic.variable.Variable;

public interface SInstruction {

    String getName();

    Label execute(ExecutionContext context);
    int cycles();
    Label getLabel();
    Variable getVariable();
}