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

public class JumpZero extends AbstractInstructionTwoLabels implements Expandable {

    public JumpZero(Variable variable, Label targetLabel) {
        this(variable, FixedLabel.EMPTY, targetLabel);
    }

    public JumpZero(Variable variable, Label label, Label targetLabel) {
        super(InstructionData.JUMP_ZERO, variable, label, targetLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        if (variableValue == 0) {
            return getTargetLabel();
        }

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = 0 GOTO " + getTargetLabel().getRepresentation();
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> parentChain = createParentChain();
        SInstruction toAdd;
        List<SInstruction> expansion = new ArrayList<>(3);
        Label L1 = context.generateLabel();
        toAdd = new JumpNotZeroInstruction(getVariable(), getLabel(), L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new GotoLabel(getTargetLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new NoOpInstruction(getVariable(), L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }


}
