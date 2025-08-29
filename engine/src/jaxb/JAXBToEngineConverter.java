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

            for (jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction : jaxbInstructions) {
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

        // Try to parse as number, otherwise treat as named variable
        try {
            int varNumber = Integer.parseInt(variableName);
            return new VariableImpl(VariableType.RESULT, varNumber);
        } catch (NumberFormatException e) {
            // For named variables, you might want to create a different type
            return new VariableImpl(VariableType.RESULT, 0);
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
