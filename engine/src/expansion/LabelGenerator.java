package expansion;

import core.logic.instruction.SInstruction;
import core.logic.label.FixedLabel;
import core.logic.label.Label;

import java.util.List;
import java.util.Set;

public class LabelGenerator {
    private Integer maxLabel = null;
    private List<SInstruction> instructions;

    public LabelGenerator(List<SInstruction> instructions) {
        this.instructions = instructions;
    }

    public Label generateLabel() {
        if (maxLabel == null) {

        }

    }

    private Integer findMaxLabel() {
        int max = 0;
        int tmp = 0;
        for (SInstruction instruction : instructions) {
            Set<Label> labels = instruction.getLabels();
            for(Label label : labels) {
                if(label != FixedLabel.EMPTY) {
                    tmp = label.getNumper();
                    if (tmp > max) {
                        max = tmp;
                    }
                }
            }

        }

        return max;



    }
}
