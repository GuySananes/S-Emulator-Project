package expansion;


import core.logic.label.LabelImpl;
import core.logic.program.SProgram;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;

import java.util.Set;

public class ZGenerator {
    private int maxZ = -1;
    private SProgram program;

    public ZGenerator(SProgram program) {
        this.program = program;
    }

    public Variable generateLabel() {
        if (maxZ == -1) {
            maxZ = findMaxLabel();
        }

        return new VariableImpl(VariableType.WORK, ++maxZ);
    }

    private int findMaxLabel() {
        Set<Variable> variables = program.getOrderedVariables();
        int max = 0;
        for (Variable variable : variables) {
            if (variable.getType() == VariableType.WORK) {
                int value = variable.getNumber();
                if (value > max) {
                    max = value;
                }
            }
        }

        return max;
    }
}
