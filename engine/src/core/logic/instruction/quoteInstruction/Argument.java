package core.logic.instruction.quoteInstruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.ExecutionResult;

public interface Argument {
    ExecutionResult evaluate(ExecutionContext context);
    String getRepresentation();
}
