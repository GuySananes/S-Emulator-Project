package DTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import core.logic.program.SProgram;
import core.logic.variable.Variable;
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
        return program.getInputVariables();
    }

    public void setInputs(List<Long> input){
        inputVariables.clear();
        inputVariables.addAll(input);
    }







}
