package run;

import core.logic.execution.Debug;
import core.logic.execution.DebugFinalResult;
import core.logic.execution.DebugResult;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.RunInputException;

import java.util.*;

public class DebugProgramDTO{
    private final SProgram program;
    private List<Long> input;
    private final Debug debug;

    public DebugProgramDTO(SProgram program){
        this.program = program;
        this.debug = new Debug(program);
        this.input = new ArrayList<>();
    }

    public void setInput(List<Long> input) throws RunInputException{
        if(input != null){
            for(Long value : input){
                if(value == null){
                    throw new RunInputException("Input values cannot be null");
                } else if(value < 0){
                    throw new RunInputException("Input values cannot be negative");
                }
            }

            this.input = input;
        }
    }

    public Set<Variable> getOrderedInputVariables(){
        return program.getOrderedInputVariablesDeepCopy();
    }

    public Set<Variable> getOrderedVariables(){
        return program.getOrderedVariablesDeepCopy();
    }

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
