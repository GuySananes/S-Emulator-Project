package core.logic.instruction;


public enum InstructionData {

    INCREASE("INCREASE", 1, "B"),
    DECREASE("DECREASE", 1, "B"),
    NO_OP("NO_OP", 0, "B"),
    JUMP_NOT_ZERO("JNZ", 3, "B");




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