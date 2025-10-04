package core.logic.instruction.quoteInstructions;

import execution.ExecutionContext;
import execution.ResultCycle;

public interface Argument {
    ResultCycle evaluate(ExecutionContext context);
    String getRepresentation();
}
