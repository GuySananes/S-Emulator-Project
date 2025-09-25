package core.logic.instruction.mostInstructions;

import core.logic.execution.ExecutionContext;
import core.logic.execution.LabelCycle;
import core.logic.instruction.InstructionData;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;
import expansion.Expandable;
import expansion.ExpansionContext;
import expansion.Utils;

import java.util.ArrayList;
import java.util.List;

public class GotoLabel extends AbstractInstructionTwoLabels implements Expandable {


    public GotoLabel(Label targetLabel) {
        this(FixedLabel.EMPTY, targetLabel);
    }

    public GotoLabel(Label label, Label targetLabel) {
        super(InstructionData.GOTO_LABEL, label, targetLabel);
    }

    @Override
    public LabelCycle execute(ExecutionContext context) {
        return new LabelCycle(getTargetLabel(), Integer.parseInt(getInstructionData().getCycleRepresentation()));

    }

    @Override
    public String getCommandRepresentation() {
        return "GOTO " + getTargetLabel().getRepresentation();
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> parentChain = createParentChain();
        SInstruction toAdd;
        List<SInstruction> expansion = new ArrayList<>(2);
        Variable z = context.generateZ();
        toAdd = new IncreaseInstruction(z, getLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);
        toAdd = new JumpNotZeroInstruction(z, getTargetLabel());
        Utils.registerInstruction(toAdd, parentChain, expansion);

        return expansion;
    }

    @Override
    public SInstruction clone() {
        return new GotoLabel(getLabel(), getTargetLabel());
    }
}
