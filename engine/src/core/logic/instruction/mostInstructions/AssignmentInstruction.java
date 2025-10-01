package core.logic.instruction.mostInstructions;

import core.logic.execution.ChangedVariable;
import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycleChangedVariable;
import core.logic.instruction.*;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.Utils;

import java.util.ArrayList;
import java.util.List;

public class AssignmentInstruction extends AbstractInstructionTwoVariables implements Expandable{

    public AssignmentInstruction(Variable variable, Variable secondaryVariable) {
        this(variable, secondaryVariable, FixedLabel.EMPTY);
    }

    public AssignmentInstruction(Variable variable, Variable secondaryVariable, Label label) {
        super(InstructionData.ASSIGNMENT, variable, secondaryVariable, label);
    }

    @Override
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        Variable toChange = getVariable();
        long oldValue = context.getVariableValue(toChange);
        long newValue = context.getVariableValue(getSecondVariable());
        context.updateVariable(toChange, newValue);
        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                Integer.parseInt(getInstructionData().getCycleRepresentation()),
                newValue == oldValue ? null :
                new ChangedVariable(toChange, oldValue, newValue));
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- " + getSecondVariable().getRepresentation();
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context){

        Label L1 = context.generateLabel();
        Label L2 = context.generateLabel();
        Label L3 = context.generateLabel();
        Variable z1 = context.generateZ();
        List<SInstruction> parentChain = createParentChain();
        SInstruction toAdd;
        List<SInstruction> expansion = new ArrayList<>(11);

        toAdd = new ZeroVariableInstruction(getVariable(), getLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpNotZeroInstruction(getSecondVariable(), L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new GotoLabel(L3);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new DecreaseInstruction(getSecondVariable(), L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new IncreaseInstruction(z1);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpNotZeroInstruction(getSecondVariable(), L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new DecreaseInstruction(z1, L2);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new IncreaseInstruction(getVariable());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new IncreaseInstruction(getSecondVariable());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpNotZeroInstruction(z1, L2);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new NoOpInstruction(getVariable(), L3);
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }

    @Override
    public SInstruction clone(){
        return new AssignmentInstruction(getVariable(), getSecondVariable(), getLabel());
    }


}
