package core.logic.program;

import core.logic.instruction.mostInstructions.SInstruction;
import java.util.List;
import java.util.Objects;

public class SFunction extends SProgramImpl{
    private final String userName;

    public SFunction(String name, String userName, SProgram originalProgram, List<SInstruction> instructions) {
        super(name, originalProgram, instructions);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public SProgram clone() {
        return new SFunction(this.name, this.userName, this.originalProgram, cloneInstructions());
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
