package run;

import core.logic.program.SProgram;
import core.logic.variable.Variable;
import exception.RunInputException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractExecuteProgramDTO {

    protected final SProgram program;
    protected List<Long> input;

    public AbstractExecuteProgramDTO(SProgram program) {
        this.program = program;
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

    public List<Long> getInput(){
        return new ArrayList<>(input);
    }

    public Set<Variable> getOrderedInputVariables(){
        return program.getOrderedInputVariablesDeepCopy();
    }

    public abstract List<Long> getOrderedInputValues();

    public Set<Variable> getOrderedVariables(){
        return program.getOrderedVariablesDeepCopy();
    }

    public abstract List<Long> getOrderedValues();


}
