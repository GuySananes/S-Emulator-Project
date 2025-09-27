
package core.logic.execution;

import exception.ProgramNotExecutedYetException;

import java.util.List;

public interface ProgramExecutor {

    ResultCycle run(List<Long> input, int degree);

    List<Long> getOrderedValues() throws ProgramNotExecutedYetException;
}
