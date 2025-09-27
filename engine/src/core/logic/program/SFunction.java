package core.logic.program;

import core.logic.instruction.mostInstructions.SInstruction;

public class SFunction extends SProgramImpl{
    private final String userName;

    public SFunction(String name, String userName, SProgram originalProgram) {
        super(name, originalProgram);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public SProgram clone() {
        SProgram clone = new SFunction(this.name, this.userName, this.originalProgram);
        for (SInstruction instruction : this.instructionList) {
            clone.addInstruction(instruction.clone());
        }

        return clone;
    }

}
