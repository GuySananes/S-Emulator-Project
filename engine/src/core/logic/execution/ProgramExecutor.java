
package core.logic.execution;

import exception.ProgramNotExecutedYetException;

import java.util.List;

public interface ProgramExecutor {

    ExecutionResult run(java.lang.Long... input);

    List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException;
}
