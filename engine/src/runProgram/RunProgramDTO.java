package runProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.DegreeOutOfRangeException;
import exception.ProgramNotExecutedYetException;
import expansion.Expansion;
import statistic.SingleRunStatisticImpl;
import statistic.StatisticManagerImpl;

public class RunProgramDTO {

    private final SProgram program;
    private int degree;
    private List<Long> input = null;
    private ProgramExecutor programExecutor;



    public RunProgramDTO(SProgram program) {
        if(program == null){
            throw new IllegalArgumentException("Program cannot be null when creating RunProgramDTO");
        }
        this.program = program;
    }

    public int getMaxDegree(){
        return program.calculateMaxDegree();
    }

    public void setDegree(int degree) throws DegreeOutOfRangeException {
        if(degree < 0 || degree > getMaxDegree()){
            throw new DegreeOutOfRangeException(0, getMaxDegree());
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

    public long runProgram(){
        SProgram program;
        if(degree > getMaxDegree()){
            throw new IllegalStateException("Degree is out of range, cannot run program");
        }
        if(degree > 0){
            program = Expansion.expand(this.program, degree);
        } else {
            program = this.program;
        }
        programExecutor = new ProgramExecutorImpl(program);
        long result = programExecutor.run(input.toArray(new Long[0]));
        program.incrementRunNumber();
        StatisticManagerImpl.getInstance().addRunStatistic(this.program,
                new SingleRunStatisticImpl(this.program.getRunNumber(),
                degree, input, result, program.calculateCycles()));

        return result;
    }

    public Set<Variable> getOrderedVariablesCopy(){
        return program.getOrderedVariablesCopy();
    }

    public List<Long> getOrderedValuesCopy() throws ProgramNotExecutedYetException {
        if(programExecutor == null){
            throw new ProgramNotExecutedYetException();
        }
        return programExecutor.getOrderedValuesCopy();
    }

    public int getCycles() {
        return program.calculateCycles();
    }








}
