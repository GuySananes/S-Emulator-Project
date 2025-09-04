package expansion;

import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;

public class ExpansionContext {
    private ZGenerator zGenerator = null;
    private LabelGenerator labelGenerator = null;
    private final SProgram program;
    private int parentIndex;

    public ExpansionContext(SProgram program) {
        this.program = program;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;
    }

    public Label generateLabel() {
        if (labelGenerator == null) {
            labelGenerator = new LabelGenerator(program);
        }
        return labelGenerator.generateLabel();
    }

    public Variable generateZ() {
        if (zGenerator == null) {
            zGenerator = new ZGenerator(program);
        }
        return zGenerator.generateLabel();
    }
}
