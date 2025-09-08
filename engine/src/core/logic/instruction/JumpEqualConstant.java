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
        List<SInstruction> parentChain = createParentChain();
        SInstruction toAdd;
        List<SInstruction> expansion = new ArrayList<>(4 + (2 * (int) constantValue));
        Variable z1 = context.generateZ();
        Label L1 = context.generateLabel();
        toAdd = new AssignmentInstruction(z1, getVariable(), getLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        for (int i = 0; i < constantValue; i++) {
            toAdd = new JumpZero(z1, L1);
            Utils.registerInstruction(toAdd, parentChain, expansion);
            toAdd = new DecreaseInstruction(z1);
            Utils.registerInstruction(toAdd, parentChain, expansion);

        }

        toAdd = new JumpNotZeroInstruction(z1, L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new GotoLabel(getTargetLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new NoOpInstruction(getVariable(), L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }
}
