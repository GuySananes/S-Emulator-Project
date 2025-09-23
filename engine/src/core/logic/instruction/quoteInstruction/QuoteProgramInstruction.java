package core.logic.instruction.quoteInstruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
import core.logic.execution.ResultCycle;
import core.logic.instruction.AbstractInstruction;
import core.logic.instruction.AssignmentInstruction;
import core.logic.instruction.InstructionData;
import core.logic.instruction.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public LabelCycle execute(ExecutionContext context) {
        ResultCycle result = functionArgument.evaluate(context);
        context.updateVariable(getVariable(), result.getResult());
        return new LabelCycle(FixedLabel.EMPTY, result.getCycles());
    }

    @Override
    public String getCommandRepresentation() {

        StringBuilder sb = new StringBuilder();
        sb.append(getVariable().getRepresentation());
        sb.append(" <- ");
        sb.append(functionArgument.getRepresentation());

        return sb.toString();
    }

    @Override
    public SInstruction clone() {
        return new QuoteProgramInstruction(getVariable(), getLabel(), functionArgument);
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        SProgram toExpand = functionArgument.getProgram();
        toExpand = toExpand.clone();
        List<SInstruction> toChange = toExpand.getInstructionList();
        Map<Variable, Variable> xToz = new HashMap<>();
        Label exit;
        //every instruction in toChange, if it has a variable x, change it to a new z
        //dave x to z in the map xToz
        //y should also be changed to a new z
        //every label should be changed to a new label
        //should consider the different instruction types. some have 1 variable, some have 2, some have labels.
        //same for labels



    }
}
