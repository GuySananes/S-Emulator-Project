package run;

import core.logic.execution.Debug;
import core.logic.execution.DebugFinalResult;
import core.logic.execution.DebugResult;
import core.logic.program.SProgram;
import exception.RunInputException;

import java.util.*;

public class DebugProgramDTO extends AbstractExecuteProgramDTO {
    private final Debug debug;

    public DebugProgramDTO(SProgram program){
        super(program);
        this.debug = new Debug(program);
    }

    @Override
    public void setInput(List<Long> input) throws RunInputException {
        super.setInput(input);
        debug.setInput(this.input);
    }

    @Override
    public List<Long> getOrderedInputValues() {
        return debug.getOrderedInputValues();
    }

    @Override
    public List<Long> getOrderedValues(){
        return debug.getOrderedValues();
    }


    public DebugResult nextStep(){
        return debug.nextStep();
    }

    public DebugFinalResult runUntilEnd() {
        return debug.resume();
    }
}
