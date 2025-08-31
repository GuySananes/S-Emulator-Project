package expansion;

import core.logic.label.Label;
import core.logic.program.SProgram;
import core.logic.variable.Variable;

public class ExpansionContext {
    private ZGenerator zGenerator = null;
    private LabelGenerator labelGenerator = null;
    private SProgram program;

    public ExpansionContext(SProgram program) {
        this.program = program;
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
