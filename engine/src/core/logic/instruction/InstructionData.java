package core.logic.instruction;


public enum InstructionData {

    INCREASE("INCREASE", 1, "B"),
    DECREASE("DECREASE", 1, "B"),
    NO_OP("NO_OP", 0, "B"),
    JUMP_NOT_ZERO("JNZ", 3, "B"),
    ZERO_VARIABLE("ZERO_VARIABLE", 1, "S"),
    GOTO_LABEL("GOTO_LABEL", 1, "S"),
    ASSIGNMENT("ASSIGNMENT", 4, "S"),
    CONSTANT_ASSIGNMENT("CONSTANT_ASSIGNMENT", 2, "S"),
    JUMP_ZERO("JUMP_ZERO", 2, "S"),
    JUMP_EQUAL_CONSTANT("JUMP_EQUAL_CONSTANT", 2, "S"),
    LUMP_EQUAL_VARIABLE("JUMP_EQUAL_VARIABLE", 2, "S");




    private final String name;
    private final int cycles;
    private final String instructionType;

    InstructionData(String name, int cycles, String instructionType) {
        this.name = name;
        this.cycles = cycles;
        this.instructionType = instructionType;
    }

    public String getName() {
        return name;
    }

    public int getCycles() {
        return cycles;
    }

    public String getInstructionType() {
        return instructionType;
    }
}