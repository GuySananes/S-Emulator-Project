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

public class AssignmentInstruction extends AbstractInstructionTwoVariables implements Expandable {

    public AssignmentInstruction(Variable variable, Variable secondaryVariable) {
        this(variable, secondaryVariable, FixedLabel.EMPTY);
    }

    public AssignmentInstruction(Variable variable, Variable secondaryVariable, Label label) {
        super(InstructionData.ASSIGNMENT, variable, secondaryVariable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long secondaryValue = context.getVariableValue(getSecondaryVariable());
        context.updateVariable(getVariable(), secondaryValue);
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- " + getSecondaryVariable().getRepresentation();
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context){

        Label L1 = context.generateLabel();
        Label L2 = context.generateLabel();
        Label L3 = context.generateLabel();
        Variable z1 = context.generateZ();

        List<SInstruction> expansion = new ArrayList<>(11);

        expansion.add(new RootedInstruction(new ZeroVariableInstruction(getVariable(), getLabel()), this));
        expansion.add(new RootedInstruction(new JumpNotZeroInstruction(getSecondaryVariable(), L1), this));
        expansion.add(new RootedInstruction(new GotoLabel(L3), this));
        expansion.add(new RootedInstruction(new DecreaseInstruction(getSecondaryVariable(), L1), this));
        expansion.add(new RootedInstruction(new IncreaseInstruction(z1), this));
        expansion.add(new RootedInstruction(new JumpNotZeroInstruction(getSecondaryVariable(), L1), this));
        expansion.add(new RootedInstruction(new DecreaseInstruction(z1, L2), this));
        expansion.add(new RootedInstruction(new IncreaseInstruction(getVariable()), this));
        expansion.add(new RootedInstruction(new IncreaseInstruction(getSecondaryVariable()), this));
        expansion.add(new RootedInstruction(new JumpNotZeroInstruction(z1, L2), this));
        expansion.add(new RootedInstruction(new NoOpInstruction(getVariable(), L3), this));

        return expansion;
    }
}
