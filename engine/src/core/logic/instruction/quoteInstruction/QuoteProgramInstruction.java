package core.logic.instruction.quoteInstruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.ExecutionResult;
import core.logic.instruction.AbstractInstruction;
import core.logic.instruction.InstructionData;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;

public class QuoteProgramInstruction extends AbstractInstruction implements Expandable {

    FunctionArgument functionArgument;

    public QuoteProgramInstruction(Variable variable, FunctionArgument functionArgument) {
        this(variable, FixedLabel.EMPTY, functionArgument);
    }

    public QuoteProgramInstruction(Variable variable, Label label, FunctionArgument functionArgument) {
        super(InstructionData.QUOTE_PROGRAM, variable, label);
        this.functionArgument = functionArgument;
    }

    @Override
    public Label execute(ExecutionContext context) {
        ExecutionResult result = functionArgument.evaluate(context);
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
