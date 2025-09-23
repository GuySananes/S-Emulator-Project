
package core.logic.execution;

import exception.ProgramNotExecutedYetException;

import java.util.List;

public interface ProgramExecutor {

    ResultCycle run(Long... input);

    List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException;
}
