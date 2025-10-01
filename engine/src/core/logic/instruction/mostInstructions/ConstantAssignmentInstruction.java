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
import java.util.Objects;

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
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        Variable toChange = getVariable();
        long oldValue = context.getVariableValue(toChange);
        long newValue = constantValue;
        context.updateVariable(getVariable(), constantValue);
        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                Integer.parseInt(getInstructionData().getCycleRepresentation()),
                newValue == oldValue ? null :
                        new ChangedVariable(toChange, oldValue, newValue));

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConstantAssignmentInstruction that = (ConstantAssignmentInstruction) o;
        return constantValue == that.constantValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), constantValue);
    }

    @Override
    public SInstruction clone() {
        return new ConstantAssignmentInstruction(constantValue, getVariable(), getLabel());
    }
}
