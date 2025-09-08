package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.Utils;

import java.util.ArrayList;
import java.util.List;

public class ConstantAssignmentInstruction extends AbstractInstruction implements Expandable {

    private final long constantValue;

    public ConstantAssignmentInstruction(long constantValue, Variable variable) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable);
        this.constantValue = constantValue;
    }

    public ConstantAssignmentInstruction(long constantValue, Variable variable, Label label) {
        super(InstructionData.CONSTANT_ASSIGNMENT, variable, label);
        this.constantValue = constantValue;
    }

    public ConstantAssignmentInstruction(Variable variable, Label label) {

        super(InstructionData.CONSTANT_ASSIGNMENT, variable, label);
        this.constantValue = 0;
    }

    public ConstantAssignmentInstruction(Variable variable) {
        this(variable, FixedLabel.EMPTY);
    }

    public long getConstantValue() {
        return constantValue;
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getVariable(), constantValue);
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {
        return getVariable().getRepresentation() + " <- " + constantValue;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> parentChain = createParentChain();
        SInstruction toAdd;

        List<SInstruction> expansion = new ArrayList<>((int) constantValue + 1);
        toAdd = new ZeroVariableInstruction(getVariable(), getLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        for (int i = 0; i < constantValue; i++) {
            toAdd = new IncreaseInstruction(getVariable());
            Utils.registerInstruction(toAdd, parentChain, expansion);

        }

        return expansion;
    }
}
