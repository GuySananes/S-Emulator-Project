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
import java.util.Set;

public class JumpEqualVariable extends AbstractInstructionTwoVariables implements Expandable {

    private final Label targetLabel;

    public JumpEqualVariable(Variable variable, Variable secondaryVariable, Label targetLabel) {
        this(variable, secondaryVariable, targetLabel, FixedLabel.EMPTY);
    }

    public JumpEqualVariable(Variable variable, Variable secondaryVariable, Label targetLabel, Label label) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, variable, secondaryVariable, label);
        this.targetLabel = targetLabel;
    }

    public Label getTargetLabel() {
        return targetLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        long secondaryValue = context.getVariableValue(getSecondaryVariable());
        if (variableValue == secondaryValue) {
            return targetLabel;
        }

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = "
                + getSecondaryVariable().getRepresentation() +
                " GOTO " + targetLabel.getRepresentation();
    }

    @Override
    public Set<Label> getLabels() {
        Set<Label> labels = super.getLabels();
        labels.add(targetLabel);
        return labels;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> parentChain = createParentChain();
        SInstruction toAdd;
        Variable z1 = context.generateZ();
        Variable z2 = context.generateZ();
        Label L1 = context.generateLabel();
        Label L2 = context.generateLabel();
        Label L3 = context.generateLabel();
        List<SInstruction> expansion = new ArrayList<>(9);

        toAdd = new AssignmentInstruction(z1, getVariable(), getLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new AssignmentInstruction(z2, getSecondaryVariable());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpZero(z1, L2, L3);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpZero(z2, L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new DecreaseInstruction(z1);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new DecreaseInstruction(z2);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new GotoLabel(L2);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpZero(z2, L3, targetLabel);
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new NoOpInstruction(getVariable(), L1);
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }
}
