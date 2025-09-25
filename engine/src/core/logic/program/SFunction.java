package core.logic.program;

import core.logic.instruction.mostInstructions.SInstruction;

public class SFunction extends SProgramImpl{
    private final String userName;

    public SFunction(String name, String userName) {
        super(name);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public SProgram clone() {
        SProgram clone = new SFunction(this.name, this.userName);
        for (SInstruction instruction : this.instructionList) {
            clone.addInstruction(instruction.clone());
        }

        return clone;
    }

}
