package DTO;

import core.logic.instruction.InstructionData;
import core.logic.label.Label;
import core.logic.variable.Variable;


//כל העניין של ההשוואות בין לייבלים, קומפר טו. כי יש גם את האינאם וגם את הלייבל אימפל, אז אני לא בטוח איך להשוות ביניהם


public class PresentInstructionDTO {

    private final InstructionData instructionData;
    private final Variable variable;
    private final Label label;

    public PresentInstructionDTO(InstructionData instructionData, Variable variable, Label label) {
        this.instructionData = instructionData;
        this.variable = variable;
        this.label = label;
    }

    public InstructionData getInstructionData() {
        return instructionData;
    }

    public Variable getVariable() {
        return variable;
    }

    public Label getLabel() {
        return label;
    }
}
