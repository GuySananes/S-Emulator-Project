package core.logic.instruction;


public enum InstructionData {

    INCREASE("INCREASE", 1),
    DECREASE("DECREASE", 1),
    NO_OP("NO_OP", 0),
    JUMP_NOT_ZERO("JNZ", 3)

    ;


    private final String name;
    private final int cycles;

    /**
     * Constructor for InstructionData enum.
     *
     * @param name   the name of the instruction
     * @param cycles the number of cycles the instruction takes to execute
     */
    InstructionData(String name, int cycles) {
        this.name = name;
        this.cycles = cycles;
    }

    /**
     * Gets the name of the instruction.
     *
     * @return the name of the instruction
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of cycles the instruction takes to execute.
     *
     * @return the number of cycles
     */
    public int getCycles() {
        return cycles;
    }
}