package run;

import core.logic.execution.Debug;
import core.logic.execution.DebugFinalResult;
import core.logic.execution.DebugResult;
import core.logic.program.SProgram;
import core.logic.variable.Variable;

import java.util.*;

public class DebugProgramDTO{
    private final SProgram program;
    private List<Long> input;
    private final Debug debug;

    public DebugProgramDTO(SProgram program){
        this.program = program;
        this.debug = new Debug(program);
        this.input = new ArrayList<>(Collections.nCopies(program.getOrderedInputVariables().size(), 0L));
    }

    public Set<Variable> getOrderedInputVariables(){
        return program.getOrderedInputVariablesDeepCopy();
    }

    public Set<Variable> getOrderedVariables(){
        return program.getOrderedVariablesDeepCopy();
    }

    public List<Long> getInputValues(){
        return input;
    }

    public void setInput(List<Long> input){
        if(input != null){
            this.input = input;
        }
    }

    public DebugResult nextStep(){
        return debug.nextStep();
    }

    public DebugFinalResult runUntilEnd() {
        return debug.resume();
    }




}
