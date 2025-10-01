package core.logic.instruction.mostInstructions;

import core.logic.execution.ChangedVariable;
import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycleChangedVariable;
import core.logic.instruction.InstructionData;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.Utils;

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
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        Variable toChange = getVariable();
        long oldValue = context.getVariableValue(toChange);
        long newValue = 0;
        context.updateVariable(toChange, newValue);
        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                Integer.parseInt(getInstructionData().getCycleRepresentation()),
                newValue == oldValue ? null :
                        new ChangedVariable(toChange, oldValue, newValue));
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- 0";
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> parentChain = createParentChain();
        SInstruction toAdd;
        List<SInstruction> expansion = new ArrayList<>(2);
        Label label = (getLabel() == FixedLabel.EMPTY ? context.generateLabel() : getLabel());
        toAdd = new DecreaseInstruction(getVariable(), label);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpNotZeroInstruction(getVariable(), label);
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }

    @Override
    public SInstruction clone() {
        return new ZeroVariableInstruction(getVariable(), getLabel());
    }

}
