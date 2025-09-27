package core.logic.program;

import core.logic.instruction.mostInstructions.SInstruction;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SFunction sFunction = (SFunction) o;
        return Objects.equals(userName, sFunction.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userName);
    }
}
