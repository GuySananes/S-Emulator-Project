package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.ExecutionResult;
import core.logic.execution.ProgramExecutorImpl;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import expansion.Expandable;

import java.util.List;

public class QuoteProgram extends AbstractInstruction implements Expandable {

    SProgram program;
    List<Argument> arguments;

    public QuoteProgram(Variable variable, SProgram program, List<Variable> input) {
        this(variable, FixedLabel.EMPTY, program, input);
    }

    public QuoteProgram(Variable variable, Label label, SProgram program, List<Variable> input) {
        super(InstructionData.QUOTE_PROGRAM, variable, label);
        this.program = program;
        this.input = input;
    }

    @Override
    public Label execute(ExecutionContext context) {
        Long[] inputValues = new Long[arguments.size()];
        // Prepare input values for the program. arguments list can have
        ProgramExecutorImpl executor = new ProgramExecutorImpl(program);
        ExecutionResult result = executor.run(inputValues);
        context.updateVariable(getVariable(), result.getResult());

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {

        StringBuilder sb = new StringBuilder();
        sb.append(getVariable().getRepresentation());
        sb.append(" <- (");
        sb.append(program.getName());
        if(!arguments.isEmpty()) {
            sb.append(",");
            for (int i = 0; i < arguments.size(); i++) {
                sb.append(arguments.get(i).getRepresentation());
                if (i < arguments.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public int getCycles() {
        return super.getCycles() + program.getCycles();
    }


}
