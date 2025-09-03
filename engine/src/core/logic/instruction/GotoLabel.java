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

public class GotoLabel extends AbstractInstructionTwoLabels implements Expandable {


    public GotoLabel(Label targetLabel) {
        this(FixedLabel.EMPTY, targetLabel);
    }

    public GotoLabel(Label label, Label targetLabel) {
        super(InstructionData.GOTO_LABEL, label, targetLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return getTargetLabel();
    }

    @Override
    public String getCommandRepresentation() {
        return "GOTO" + getTargetLabel().getRepresentation();
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expansion = new ArrayList<>(2);
        Variable z = context.generateZ();
        expansion.add(new RootedInstruction(new IncreaseInstruction(z, getLabel()), this));
        expansion.add(new RootedInstruction(new JumpNotZeroInstruction(z, getTargetLabel()), this));

        return expansion;
    }
}
