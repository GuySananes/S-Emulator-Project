package core.logic.instruction.mostInstructions;

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
import java.util.Set;

public class JumpEqualVariable extends AbstractInstructionTwoVariables implements Expandable {

    private Label targetLabel;

    public JumpEqualVariable(Variable variable, Variable secondaryVariable, Label targetLabel) {
        this(variable, secondaryVariable, targetLabel, FixedLabel.EMPTY);
    }

    public JumpEqualVariable(Variable variable, Variable secondaryVariable, Label targetLabel, Label label) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, variable, secondaryVariable, label);
        this.targetLabel = targetLabel;
    }

    public void setTargetLabel(Label targetLabel) {
        this.targetLabel = targetLabel;
    }

    public Label getTargetLabel() {
        return targetLabel;
    }

    public Label getTargetLabelDeepCopy() {
        return targetLabel.deepCopy();
    }

    @Override
    public LabelCycleChangedVariable execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        long secondaryValue = context.getVariableValue(getSecondVariable());
        if (variableValue == secondaryValue) {
            return new LabelCycleChangedVariable(targetLabel,
                    Integer.parseInt(getInstructionData().getCycleRepresentation()),
                    null);
        }

        return new LabelCycleChangedVariable(FixedLabel.EMPTY,
                Integer.parseInt(getInstructionData().getCycleRepresentation()),
                null);
    }

    @Override
    public String getCommandRepresentation() {
        return "IF " + getVariable().getRepresentation() + " = "
                + getSecondVariable().getRepresentation() +
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
        toAdd = new AssignmentInstruction(z2, getSecondVariable());
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JumpEqualVariable that = (JumpEqualVariable) o;
        return Objects.equals(targetLabel, that.targetLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), targetLabel);
    }

    @Override
    public SInstruction clone() {
        return new JumpEqualVariable(getVariable(), getSecondVariable(), targetLabel, getLabel());
    }
}
