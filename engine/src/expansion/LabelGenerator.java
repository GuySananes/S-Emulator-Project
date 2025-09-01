package expansion;

import core.logic.instruction.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;
import core.logic.label.LabelImpl;
import core.logic.program.SProgram;

import java.util.List;
import java.util.Set;

public class LabelGenerator {
    private int maxLabel = -1;
    private SProgram program;

    public LabelGenerator(SProgram program) {
        this.program = program;
    }

    public Label generateLabel() {
        if (maxLabel == -1) {
            maxLabel = findMaxLabel();
        }

        return new LabelImpl(++maxLabel);
    }

    private int findMaxLabel() {
        Set<Label> labels = program.getOrderedLabels();
        int max = 0;
        for (Label label : labels) {
            if (label instanceof LabelImpl) {
                int value = ((LabelImpl) label).getNumber();
                if (value > max) {
                    max = value;
                }
            }
        }

        return max;
    }
}
