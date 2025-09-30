package run;

import java.util.List;
import java.util.Set;

import core.logic.execution.ResultCycle;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.ProgramNotExecutedYetException;
import exception.RunInputException;

public class RunProgramDTO {

    private final SProgram program;
/*
    private SProgram expandedProgram = null;
*/
    private List<Long> input = null;
    private ProgramExecutor programExecutor = null;



    public RunProgramDTO(SProgram program) {
        this.program = program;
    }

/*    public int getMaxDegree(){
        return program.getDegree();
    }

    public int getMinDegree(){
        return program.getMinDegree();
    }

    public void setDegree(int degree) throws DegreeOutOfRangeException {
        if(degree < program.getMinDegree() || degree > getMaxDegree()){
            throw new DegreeOutOfRangeException(getMinDegree() , getMaxDegree());
        }
        this.degree = degree;
    }*/

    public Set<Variable> getInputs(){
        return program.getOrderedInputVariablesDeepCopy();
    }

    public void setInput(List<Long> input) throws RunInputException {
        for(Long value : input){
            if(value == null){
                throw new RunInputException("Input values cannot be null");
            } else if(value < 0){
                throw new RunInputException("Input values cannot be negative");
            }
        }

        this.input = input;
    }

    public ResultCycle runProgram(){
        ResultCycle result;
        /*SProgram progToRun = this.program;*/
/*        if(degree > 0){
            expandedProgram = Expansion.expand(this.program, degree);
            progToRun = expandedProgram;
        }*/

        programExecutor = new ProgramExecutorImpl(/*progToRun*/program);
        result = programExecutor.run(input);

        return result;
    }

    public Set<Variable> getOrderedVariablesCopy() throws ProgramNotExecutedYetException{
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException(program.getName());
        }
/*
        return Objects.requireNonNullElse(expandedProgram, program).getOrderedVariablesDeepCopy();
*/
        return program.getOrderedVariablesDeepCopy();
    }

    public List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException(program.getName());
        }
        return programExecutor.getOrderedValues();
    }

/*    public PresentProgramDTO getPresentProgramDTO()throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }

        return new PresentProgramDTO(Objects.requireNonNullElse(expandedProgram, program));
    }*/








}
