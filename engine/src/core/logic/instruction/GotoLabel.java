package core.logic.instruction;

import core.logic.execution.ExecutionContext;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.variable.Variable;

import java.util.Set;

public class GotoLabel extends AbstractInstructionTwoLabels{


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
}
