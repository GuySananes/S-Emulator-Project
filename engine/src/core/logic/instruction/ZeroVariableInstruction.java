package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.RootedInstruction;

import java.util.ArrayList;
import java.util.List;

public class ZeroVariableInstruction extends AbstractInstruction implements Expandable {

    public ZeroVariableInstruction(Variable variable) {
        super(InstructionData.ZERO_VARIABLE, variable);
    }

    public ZeroVariableInstruction(Variable variable, Label label) {
        super(InstructionData.ZERO_VARIABLE, variable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), 0);
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- 0";
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        IndexedInstruction parentInstruction = new IndexedInstruction(context.getParentIndex(), this);
        List<SInstruction> expansion = new ArrayList<>(2);
        Label label = (getLabel() == FixedLabel.EMPTY ? context.generateLabel() : getLabel());
        expansion.add(new RootedInstruction(new DecreaseInstruction(getVariable(), label), parentInstruction));
        expansion.add(new RootedInstruction(new JumpNotZeroInstruction(getVariable(), label), parentInstruction));

        return expansion;
    }

}
