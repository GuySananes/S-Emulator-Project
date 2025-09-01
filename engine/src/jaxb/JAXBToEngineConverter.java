package jaxb;

import jaxb.engine.src.jaxb.schema.generated.*;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import core.logic.instruction.SInstruction;
import core.logic.instruction.*;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;
import core.logic.label.Label;
import core.logic.label.LabelImpl;

import java.util.List;

public class JAXBToEngineConverter {

    /**
     * Collects all defined labels from the list of JAXB instructions.
     * Any instruction with an S-Label defines that label as a jump target.
     * 
     * @param jaxbInstructions List of JAXB instructions
     * @return Set of defined label names
     */
    private static java.util.Set<String> collectDefinedLabels(List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions) {
        java.util.Set<String> definedLabels = new java.util.HashSet<>();
        for (jaxb.engine.src.jaxb.schema.generated.SInstruction instruction : jaxbInstructions) {
            // Any instruction with S-Label defines that label
            if (instruction.getSLabel() != null && !instruction.getSLabel().isEmpty()) {
                definedLabels.add(instruction.getSLabel());
            }
        }
        return definedLabels;
    }

    /**
     * Validates that a single JAXB instruction's label references are valid.
     * 
     * @param jaxbInstruction The JAXB instruction to validate
     * @param definedLabels Set of all defined labels in the program
     * @throws IllegalArgumentException if a referenced label is not found
     */
    private static void validateInstructionLabelReferences(
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
            java.util.Set<String> definedLabels) {

        String instructionName = jaxbInstruction.getName();
        String referencedLabel = getReferencedLabel(jaxbInstruction);

        // Instructions that use labels as jump targets
        if (isLabelReferencingInstruction(instructionName)) {
            if (referencedLabel == null || referencedLabel.isEmpty()) {
                throw new IllegalArgumentException(
                    "Instruction " + instructionName + " requires a label but none was provided"
                );
            }

            // Allow special system labels like "EXIT"
            if (isSystemLabel(referencedLabel)) {
                return; // System labels don't need to be defined in the program
            }

            if (!definedLabels.contains(referencedLabel)) {
                throw new IllegalArgumentException(
                    "Instruction " + instructionName + " references undefined label: " + referencedLabel + 
                    ". Available labels: " + definedLabels
                );
            }
        }
    }

    /**
     * Extracts the referenced label from a JAXB instruction's arguments.
     * This finds the label that the instruction wants to jump to, not the label that defines this instruction.
     * 
     * @param jaxbInstruction The JAXB instruction
     * @return The referenced label name, or null if none found
     */
    private static String getReferencedLabel(jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction) {
        // Check instruction arguments for label references (jump targets)
        if (jaxbInstruction.getSInstructionArguments() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SInstructionArgument arg : 
                 jaxbInstruction.getSInstructionArguments().getSInstructionArgument()) {

                if ("gotoLabel".equals(arg.getName()) || "JNZLabel".equals(arg.getName()) || 
                    "JZLabel".equals(arg.getName()) || "jumpLabel".equals(arg.getName())) {
                    return arg.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Checks if the given instruction type requires a valid label reference.
     * 
     * @param instructionName The name of the instruction
     * @return true if the instruction references a label that must exist
     */
    private static boolean isLabelReferencingInstruction(String instructionName) {
        switch (instructionName) {
            case "GOTO_LABEL":
            case "JUMP_NOT_ZERO":
            case "JUMP_ZERO":
            case "JUMP_EQUAL_CONSTANT":
            case "JUMP_EQUAL_VARIABLE":
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if the given label is a system-defined label that doesn't need
     * to be defined in the program.
     * 
     * @param labelName The name of the label to check
     * @return true if it's a system label
     */
    private static boolean isSystemLabel(String labelName) {
        return labelName.equals("EXIT");
    }

    public static SProgram convertJAXBToEngine(jaxb.engine.src.jaxb.schema.generated.SProgram jaxbProgram) {
        if (jaxbProgram == null) {
            throw new IllegalArgumentException("JAXB program cannot be null");
        }

        // Create the real engine program
        SProgram engineProgram = new SProgramImpl(jaxbProgram.getName());

        // Convert instructions
        if (jaxbProgram.getSInstructions() != null) {
            List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions = jaxbProgram.getSInstructions()
                    .getSInstruction();

            // Collect all defined labels first for validation
            java.util.Set<String> definedLabels = collectDefinedLabels(jaxbInstructions);

            for (jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction : jaxbInstructions) {
                // Validate each instruction's label references
                validateInstructionLabelReferences(jaxbInstruction, definedLabels);

                SInstruction engineInstruction = convertInstruction(jaxbInstruction);
                if (engineInstruction != null) {
                    engineProgram.addInstruction(engineInstruction);
                }
            }
        }
        return engineProgram;
    }

    private static SInstruction convertInstruction(jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction) {
        if (jaxbInstruction == null) {
            return null;
        }

        String instructionName = jaxbInstruction.getName();
        String variableName = jaxbInstruction.getSVariable();

        // Create variable
        Variable variable = createVariable(variableName);

        // Create a label if present
        Label label = null;
        if (jaxbInstruction.getSLabel() != null && !jaxbInstruction.getSLabel().isEmpty()) {
            // Try to parse as a number, otherwise use a default
            try {
                int labelNumber = Integer.parseInt(jaxbInstruction.getSLabel());
                label = new LabelImpl(labelNumber);
            } catch (NumberFormatException e) {
                // For named labels, create a default numbered label
                label = new LabelImpl(0);
            }
        }

        // Create instruction based on name
        switch (instructionName) {
            case "INCREASE":
                return label != null ? new IncreaseInstruction(variable, label) : new IncreaseInstruction(variable);

            case "DECREASE":
                return label != null ? new DecreaseInstruction(variable, label) : new DecreaseInstruction(variable);

            case "ZERO_VARIABLE":
                return label != null ? new ZeroVariableInstruction(variable, label)
                        : new ZeroVariableInstruction(variable);

            case "GOTO_LABEL":
                return label != null ? new GotoLabel(label) : new GotoLabel(new LabelImpl(0));

            case "JUMP_NOT_ZERO":
                return label != null ? new JumpNotZeroInstruction(variable, label)
                        : new JumpNotZeroInstruction(variable, new LabelImpl(0));

            case "JUMP_ZERO":
                return label != null ? new JumpZero(variable, label) : new JumpZero(variable, new LabelImpl(0));

            case "ASSIGNMENT":
                return createAssignmentInstruction(variable, jaxbInstruction, label);

            case "CONSTANT_ASSIGNMENT":
                return createConstantAssignmentInstruction(variable, jaxbInstruction, label);

            case "JUMP_EQUAL_CONSTANT":
                return createJumpEqualConstant(variable, jaxbInstruction, label);

            case "JUMP_EQUAL_VARIABLE":
                return createJumpEqualVariable(variable, jaxbInstruction, label);

            default:
                System.err.println("Unknown instruction: " + instructionName);
                return new NoOpInstruction(variable);
        }
    }

    private static Variable createVariable(String variableName) {
        if (variableName == null || variableName.isEmpty()) {
            return new VariableImpl(VariableType.RESULT, 0);
        }

        // Parse variable name to determine type and number
        if (variableName.equals("y")) {
            // The result variable is always y with the number 0
            return new VariableImpl(VariableType.RESULT, 0);
        } else if (variableName.startsWith("x")) {
            // Input variables: x1, x2, x3, etc.
            try {
                String numberStr = variableName.substring(1); // Remove 'x' prefix
                int varNumber = Integer.parseInt(numberStr);
                return new VariableImpl(VariableType.INPUT, varNumber);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid input variable format: " + variableName + 
                                                 ". Expected format: x<number> (e.g., x1, x2)");
            }
        } else if (variableName.startsWith("z")) {
            // Work variables: z1, z2, z3, etc.
            try {
                String numberStr = variableName.substring(1); // Remove 'z' prefix
                int varNumber = Integer.parseInt(numberStr);
                return new VariableImpl(VariableType.WORK, varNumber);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid work variable format: " + variableName + 
                                                 ". Expected format: z<number> (e.g., z1, z2)");
            }
        } else {
            throw new IllegalArgumentException("Unknown variable format: " + variableName + 
                                             ". Expected formats: y, x<number>, or z<number>");
        }
    }

    private static SInstruction createAssignmentInstruction(Variable variable,
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        // Assignment instruction needs a secondary variable
        // For now, create a default secondary variable
        Variable secondaryVariable = new VariableImpl(VariableType.RESULT, 0);
        return label != null ? new AssignmentInstruction(variable, secondaryVariable, label)
                : new AssignmentInstruction(variable, secondaryVariable);
    }

    private static SInstruction createConstantAssignmentInstruction(Variable variable,
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        return label != null ? new ConstantAssignmentInstruction(variable, label)
                : new ConstantAssignmentInstruction(variable);
    }

    private static SInstruction createJumpEqualConstant(Variable variable,
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        // Jump if equal constant needs a label
        if (label != null) {
            return new JumpEqualConstant(variable, label);
        }
        return new NoOpInstruction(variable); // Fallback
    }

    private static SInstruction createJumpEqualVariable(Variable variable,
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        // Jump if equal variable needs a label
        if (label != null) {
            return new JumpEqualVariable(variable, label);
        }
        return new NoOpInstruction(variable); // Fallback
    }
}
