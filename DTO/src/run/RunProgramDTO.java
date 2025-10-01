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

public class RunProgramDTO {

    private final SProgram program;
    private List<Long> input;
    private ProgramExecutor programExecutor = null;

    public RunProgramDTO(SProgram program) {
        this.program = program;
        programExecutor = new ProgramExecutor(program);
        this.input = new ArrayList<>();
    }

    public void setInput(List<Long> input) throws RunInputException {
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

    public Set<Variable> getOrderedVariables() {
        return program.getOrderedVariablesDeepCopy();
    }

    public List<Long> getOrderedValues() throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException(program.getName());
        }
        return programExecutor.getOrderedValues();
    }

    public ResultCycle runProgram(){
        return programExecutor.run(input);
    }



}
