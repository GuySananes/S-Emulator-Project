package core.logic.architecture;

import present.mostInstructions.PresentInstructionDTO;

import java.util.*;

public enum Architecture {
    I("I", 5, Set.of("NO_OP", "INCREASE", "DECREASE", "JNZ")),

    II("II", 100, Set.of("NO_OP", "INCREASE", "DECREASE", "JNZ",
            "ZERO_VARIABLE", "CONSTANT_ASSIGNMENT", "GOTO_LABEL")),

    III("III", 500, Set.of("NO_OP", "INCREASE", "DECREASE", "JNZ",
            "ZERO_VARIABLE", "CONSTANT_ASSIGNMENT", "GOTO_LABEL",
            "ASSIGNMENT", "JUMP_ZERO", "JUMP_EQUAL_CONSTANT", "JUMP_EQUAL_VARIABLE")),

    IV("IV", 1000, Set.of("NO_OP", "INCREASE", "DECREASE", "JNZ",
            "ZERO_VARIABLE", "CONSTANT_ASSIGNMENT", "GOTO_LABEL",
            "ASSIGNMENT", "JUMP_ZERO", "JUMP_EQUAL_CONSTANT", "JUMP_EQUAL_VARIABLE",
            "QUOTE_PROGRAM", "JUMP_EQUAL_FUNCTION"));

    private final String name;
    private final int creditCost;
    private final Set<String> supportedInstructions;

    Architecture(String name, int creditCost, Set<String> supportedInstructions) {
        this.name = name;
        this.creditCost = creditCost;
        this.supportedInstructions = supportedInstructions;
    }

    public String getName() {
        return name;
    }

    public int getCreditCost() {
        return creditCost;
    }

    public Set<String> getSupportedInstructions() {
        return Collections.unmodifiableSet(supportedInstructions);
    }

    /**
     * Check if this architecture supports a given instruction
     */
    public boolean supportsInstruction(String instructionName) {
        return supportedInstructions.contains(instructionName);
    }


    /**
     * Get unsupported instructions in a program
     */
    public List<String> getUnsupportedInstructions(present.program.PresentProgramDTO program) {
        List<String> unsupported = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // Track unique instruction types

        System.out.println("=== ARCHITECTURE VALIDATION ===");
        System.out.println("Architecture: " + this.name);
        System.out.println("Supported instructions: " + this.supportedInstructions);

        for (PresentInstructionDTO instruction : program.getInstructionList()) {
            if (instruction.getInstructionData() == null) {
                System.out.println("WARNING: Instruction has no InstructionData: " + instruction.getRepresentation());
                continue;
            }

            // Get the instruction type name from the enum
            String instructionType = instruction.getInstructionData().name();

            System.out.println("Checking instruction: " + instruction.getRepresentation() +
                    " -> Type: " + instructionType +
                    " -> Supported: " + supportsInstruction(instructionType));

            // Check if supported and add to unsupported list if not
            if (!supportsInstruction(instructionType)) {
                if (!seen.contains(instructionType)) {
                    unsupported.add(instructionType);
                    seen.add(instructionType);
                    System.out.println("  ❌ UNSUPPORTED: " + instructionType);
                }
            }
        }

        System.out.println("Total unsupported instructions found: " + unsupported);
        System.out.println("=================================");

        return unsupported;
    }

    /**
     * Find minimum architecture required for a list of instructions
     */
    public static Architecture findMinimumRequired(List<String> instructionNames) {
        for (Architecture arch : values()) {
            boolean allSupported = true;
            for (String instructionName : instructionNames) {
                if (!arch.supportsInstruction(instructionName)) {
                    allSupported = false;
                    break;
                }
            }
            if (allSupported) {
                return arch;
            }
        }
        return IV; // Default to highest
    }

    /**
     * Parse architecture from string
     */
    public static Architecture fromString(String name) {
        if (name == null) {
            return null;
        }

        for (Architecture arch : values()) {
            if (arch.name.equalsIgnoreCase(name)) {
                return arch;
            }
        }

        return null;
    }
}