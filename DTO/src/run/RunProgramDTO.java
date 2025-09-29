package run;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import core.logic.execution.ResultCycle;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.DegreeOutOfRangeException;
import exception.ProgramNotExecutedYetException;
import exception.RunInputException;
import expansion.Expansion;
import present.program.PresentProgramDTO;

public class RunProgramDTO {

    private final SProgram program;
    private SProgram expandedProgram = null;
    private int degree = 0;
    private List<Long> input = null;
    private ProgramExecutor programExecutor = null;



    public RunProgramDTO(SProgram program) {
        if(program == null){
            throw new IllegalArgumentException("Program cannot be null when creating RunProgramDTO");
        }
        this.program = program;
    }

    public int getMaxDegree(){
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
    }

    public Set<Variable> getInputs(){
        return program.getOrderedInputVariablesDeepCopy();
    }

    public void setInputs(List<Long> input) throws RunInputException {
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
        SProgram progToRun = this.program;
        if(degree > 0){
            expandedProgram = Expansion.expand(this.program, degree);
            progToRun = expandedProgram;
        }

        programExecutor = new ProgramExecutorImpl(progToRun);
        result = programExecutor.run(input, degree);

        return result;
    }

    public Set<Variable> getOrderedVariablesCopy() throws ProgramNotExecutedYetException{
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }
        return Objects.requireNonNullElse(expandedProgram, program).getOrderedVariablesDeepCopy();
    }

    public List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }
        return programExecutor.getOrderedValues();
    }

    public PresentProgramDTO getPresentProgramDTO()throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }

        return new PresentProgramDTO(Objects.requireNonNullElse(expandedProgram, program));
    }








}
