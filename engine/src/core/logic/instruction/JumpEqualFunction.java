package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
import core.logic.execution.ResultCycle;
import core.logic.instruction.quoteInstruction.FunctionArgument;
import core.logic.instruction.quoteInstruction.QuoteProgramInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.Utils;

import java.util.ArrayList;
import java.util.List;

public class JumpEqualFunction extends AbstractInstructionTwoLabels implements Expandable {

    private final FunctionArgument functionArgument;

    public JumpEqualFunction(Variable variable, Label label, Label targetLabel, FunctionArgument functionArgument) {
        super(InstructionData.Jump_Equal_Function, variable, label, targetLabel);
        this.functionArgument = functionArgument;
    }

    public JumpEqualFunction(Variable variable, Label targetLabel, FunctionArgument functionArgument) {
        this(variable, FixedLabel.EMPTY, targetLabel, functionArgument);
    }

    @Override
    public LabelCycle execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        ResultCycle resultCycle = functionArgument.evaluate(context);

        if (variableValue == resultCycle.getResult()) {
            return new LabelCycle(getTargetLabel(), resultCycle.getCycles());
        }

        return new LabelCycle(FixedLabel.EMPTY, resultCycle.getCycles());
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> parentChain = createParentChain();
        List<SInstruction> expansion = new ArrayList<>(2);
        Variable z = context.generateZ();
        SInstruction toAdd = new QuoteProgramInstruction(z, getLabel(), functionArgument);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpEqualVariable(getVariable(), z, getTargetLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }

    @Override
    public SInstruction clone() {
        return new JumpEqualFunction(getVariable(), getLabel(), getTargetLabel(), functionArgument);
    }

    @Override
    protected String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = " + functionArgument.getRepresentation() + " GOTO " + getTargetLabel().getRepresentation();
    }
}
