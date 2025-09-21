package core.logic.instruction;


public enum InstructionData {

    INCREASE("INCREASE", 1, "B", 0),
    DECREASE("DECREASE", 1, "B", 0),
    NO_OP("NO_OP", 0, "B", 0),
    JUMP_NOT_ZERO("JNZ", 3, "B", 0),
    ZERO_VARIABLE("ZERO_VARIABLE", 1, "S", 1),
    GOTO_LABEL("GOTO_LABEL", 1, "S", 1),
    ASSIGNMENT("ASSIGNMENT", 4, "S", 2),
    CONSTANT_ASSIGNMENT("CONSTANT_ASSIGNMENT", 2, "S", 2),
    JUMP_ZERO("JUMP_ZERO", 2, "S", 2),
    JUMP_EQUAL_CONSTANT("JUMP_EQUAL_CONSTANT", 2, "S", 3),
    JUMP_EQUAL_VARIABLE("JUMP_EQUAL_VARIABLE", 2, "S", 3),
    QUOTE_PROGRAM("QUOTE_PROGRAM", 5, "S", -1);




    private final String name;
    private final int cycles;
    private final String instructionType;
    private final int degree;

    InstructionData(String name, int cycles, String instructionType, int degree) {
        this.name = name;
        this.cycles = cycles;
        this.instructionType = instructionType;
        this.degree = degree;
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

    public int getDegree() { return degree; }
}