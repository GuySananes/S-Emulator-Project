
package core.logic.execution;

import core.logic.variable.Variable;
import exception.ProgramNotExecutedYetException;

import java.util.List;
import java.util.Map;

public interface ProgramExecutor {

    long run(java.lang.Long... input);

    public List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException;
}
