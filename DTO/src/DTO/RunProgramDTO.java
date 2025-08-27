package DTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import core.logic.execution.ExecutionContext;
import core.logic.execution.ExecutionContextImpl;
import core.logic.execution.ProgramExecutor;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import core.logic.variable.VariableType;
import exception.DegreeOutOfRangeException;

public class RunProgramDTO {

    private final SProgram program;
    private int degree;
    private final List<Long> inputVariables;
    public RunProgramDTO(SProgram program) {
        if(program == null){
            throw new IllegalArgumentException("Program cannot be null when creating RunProgramDTO");
        }
        this.program = program;
        inputVariables = new ArrayList<>();
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
        inputVariables.clear();
        inputVariables.addAll(input);
    }

    public long runProgram(){
        SProgram program;
        ProgramExecutor programExecutor;

        if(degree > getMaxDegree()){
            throw new IllegalStateException("Degree is out of range, cannot run program");
        }

        if(degree > 0){
            program = extendProgramToDegree(degree);
        } else {
            program = this.program;
        }

        programExecutor = new ProgramExecutorImpl(program);
        return programExecutor.run(inputVariables.toArray(new Long[0]));
    }

    public Set<Variable> getOrderedVariablesCopy(){
        return program.getOrderedVariablesCopy();
    }

    public int getCycles() {
        return program.calculateCycles();
    }








}
