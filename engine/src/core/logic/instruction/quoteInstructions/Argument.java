package core.logic.instruction.quoteInstructions;

import core.logic.execution.ExecutionContext;
import core.logic.execution.ResultCycle;

public interface Argument {
    ResultCycle evaluate(ExecutionContext context);
    String getRepresentation();
}
