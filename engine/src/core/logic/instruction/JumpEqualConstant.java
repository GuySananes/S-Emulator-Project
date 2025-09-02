package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.RootedInstruction;

import java.util.List;

public class JumpEqualConstant extends AbstractInstructionTwoLabels implements Expandable {

    private final long constantValue;

    public JumpEqualConstant(Variable variable, long constantValue, Label targetLabel) {
        this(variable, constantValue, FixedLabel.EMPTY, targetLabel);
    }

    public JumpEqualConstant(Variable variable, long constantValue, Label label, Label targetLabel) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, variable, label, targetLabel);
        this.constantValue = constantValue;
    }

    public JumpEqualConstant(Variable variable, Label label) {
        this(variable, 0, label);
    }

    public long getConstantValue() {
        return constantValue;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        if (variableValue == constantValue) {
            return getTargetLabel();
        }

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = " + constantValue + " GOTO " + getTargetLabel().getRepresentation();
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {

        List<SInstruction> expansion = new java.util.ArrayList<>(4 + (2 * (int) constantValue));
        Variable z1 = context.generateZ();
        Label L1 = context.generateLabel();
        expansion.add(new RootedInstruction(new AssignmentInstruction(z1, getVariable(), getLabel()), this));
        for (int i = 0; i < constantValue; i++) {
            expansion.add(new RootedInstruction(new JumpZero(z1, L1), this));
            expansion.add(new RootedInstruction(new DecreaseInstruction(z1), this));
        }
        expansion.add(new RootedInstruction(new JumpNotZeroInstruction(z1, L1), this));
        expansion.add(new RootedInstruction(new GotoLabel(getTargetLabel()), this));
        expansion.add(new RootedInstruction(new NoOpInstruction(getVariable(), L1), this));

        return expansion;
    }
}
