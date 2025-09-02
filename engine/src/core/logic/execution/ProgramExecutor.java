
package core.logic.execution;

import exception.ProgramNotExecutedYetException;

import java.util.List;

public interface ProgramExecutor {

    long run(java.lang.Long... input);

    public List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException;
}
