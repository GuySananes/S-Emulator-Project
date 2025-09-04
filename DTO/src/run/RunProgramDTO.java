package run;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import core.logic.execution.ExecutionResult;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.DegreeOutOfRangeException;
import exception.ProgramNotExecutedYetException;
import expansion.Expansion;
import present.PresentProgramDTO;
import present.PresentProgramDTOCreator;
import statistic.SingleRunStatisticImpl;
import statistic.StatisticManagerImpl;

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
        return program.calculateMaxDegree();
    }

    public int getMinDegree(){
        return program.getMinDegree();
    }

    public void setDegree(int degree) throws DegreeOutOfRangeException {
        if(degree < 0 || degree > getMaxDegree()){
            throw new DegreeOutOfRangeException(getMinDegree() , getMaxDegree());
        }
        this.degree = degree;

    }

    public Set<Variable> getInputs(){
        return program.getInputVariablesCopy();
    }

    public void setInputs(List<Long> input){
        if(input == null){
            throw new IllegalArgumentException("Input list cannot be null when setting inputs");
        }

        this.input = new ArrayList<>(input);
    }

    public ExecutionResult runProgram(){
        ExecutionResult result;
        SProgram program = this.program;
        if(degree > 0){
            expandedProgram = Expansion.expand(this.program, degree);
            program = expandedProgram;
        }

        programExecutor = new ProgramExecutorImpl(program);
        result = programExecutor.run(input.toArray(new Long[0]));
        this.program.incrementRunNumber();
        StatisticManagerImpl.getInstance().addRunStatistic(this.program,
                new SingleRunStatisticImpl(this.program.getRunNumber(),
                degree, input, result.getResult(), result.getCycles()));

        return result;
    }

    public Set<Variable> getOrderedVariablesCopy() throws ProgramNotExecutedYetException{
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }
        return Objects.requireNonNullElse(expandedProgram, program).getOrderedVariablesCopy();
    }

    public List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }
        return programExecutor.getOrderedValuesCopy();
    }

    public int getCycles() throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }
        return Objects.requireNonNullElse(expandedProgram, program).calculateCycles();
    }

    public SProgram getPresentProgramDTO()throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }
        return Objects.requireNonNullElse(expandedProgram, program);
    }








}
