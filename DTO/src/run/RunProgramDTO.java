package run;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import core.logic.execution.ProgramExecutor;
import core.logic.execution.ResultCycle;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.ProgramNotExecutedYetException;
import exception.RunInputException;

public class RunProgramDTO extends AbstractRunProgramDTO{

    private ProgramExecutor programExecutor;

    public RunProgramDTO(SProgram program) {
        super(program);
        programExecutor = new ProgramExecutor(program);
    }

    @Override
    public List<Long> getOrderedInputValues() {
        return  programExecutor.getOrderedInputValues();
    }

    @Override
    public List<Long> getOrderedValues() {
        return programExecutor.getOrderedValues();
    }

    public ResultCycle runProgram(){
        return programExecutor.run(input);
    }



}
