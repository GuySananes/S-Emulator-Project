package run;

import java.util.List;

import core.logic.execution.ProgramExecutor;
import core.logic.execution.ResultCycle;
import core.logic.program.SProgram;

public class RunProgramDTO extends AbstractExecuteProgramDTO {

    private final ProgramExecutor programExecutor;

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
