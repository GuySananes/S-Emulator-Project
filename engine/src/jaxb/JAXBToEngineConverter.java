package jaxb;

import core.logic.instruction.mostInstructions.*;
import core.logic.instruction.quoteInstructions.Argument;
import core.logic.instruction.quoteInstructions.FunctionArgument;
import core.logic.instruction.quoteInstructions.JumpEqualFunction;
import core.logic.instruction.quoteInstructions.QuoteProgramInstruction;
import core.logic.label.Label;
import core.logic.label.LabelImpl;
import core.logic.program.SFunction;
import core.logic.program.SProgram;
import core.logic.program.SProgramImpl;
import core.logic.variable.Variable;
import core.logic.variable.VariableImpl;
import core.logic.variable.VariableType;
import exception.ProgramValidationException;
import jaxb.engine.src.jaxb.schema.generated.SInstructionArgument;
import jaxb.engine.src.jaxb.schema.generated.SInstructionArguments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JAXBToEngineConverter {

    private static java.util.Set<String> collectDefinedLabels(List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions) {
        java.util.Set<String> definedLabels = new java.util.HashSet<>();
        for (jaxb.engine.src.jaxb.schema.generated.SInstruction instruction : jaxbInstructions) {
            if (instruction.getSLabel() != null && !instruction.getSLabel().isEmpty()) {
                definedLabels.add(instruction.getSLabel());
            }
        }
        return definedLabels;
    }

    private static void validateInstructionLabelReferences(
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
            java.util.Set<String> definedLabels) throws ProgramValidationException {

        String instructionName = jaxbInstruction.getName();
        String referencedLabel = getReferencedLabel(jaxbInstruction);

        if (isLabelReferencingInstruction(instructionName)) {
            if (referencedLabel == null || referencedLabel.isEmpty()) {
                throw new ProgramValidationException(
                        "Instruction " + instructionName + " requires a label but none was provided"
                );
            }

            if (isSystemLabel(referencedLabel)) {
                return;
            }

            if (!definedLabels.contains(referencedLabel)) {
                throw new ProgramValidationException(
                        "Instruction " + instructionName + " references undefined label: " + referencedLabel +
                                ". Available labels: " + definedLabels
                );
            }
        }
    }

    private static String getReferencedLabel(jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction) {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SInstructionArgument arg :
                    jaxbInstruction.getSInstructionArguments().getSInstructionArgument()) {

                if ("gotoLabel".equals(arg.getName()) || "JNZLabel".equals(arg.getName()) ||
                        "JZLabel".equals(arg.getName()) || "jumpLabel".equals(arg.getName()) ||
                        "JEConstantLabel".equals(arg.getName()) || "JEVariableLabel".equals(arg.getName()) ||
                        "JEFunctionLabel".equals(arg.getName())) {
                    return arg.getValue();
                }
            }
        }
        return null;
    }

    private static boolean isLabelReferencingInstruction(String instructionName) {
        switch (instructionName) {
            case "GOTO_LABEL":
            case "JUMP_NOT_ZERO":
            case "JUMP_ZERO":
            case "JUMP_EQUAL_CONSTANT":
            case "JUMP_EQUAL_VARIABLE":
            case "JUMP_EQUAL_FUNCTION":
                return true;
            default:
                return false;
        }
    }

    private static boolean isSystemLabel(String labelName) {
        return labelName.equals("EXIT");
    }

    private static void validateMainProgramFunctionReferences(
            List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            java.util.Set<String> systemFunctionNames) throws ProgramValidationException {

        for (jaxb.engine.src.jaxb.schema.generated.SInstruction instruction : jaxbInstructions) {
            validateInstructionFunctionReferences(instruction, jaxbFunctionMap, systemFunctionNames, "main program");
        }
    }

    private static void validateFunctionReferences(
            jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            java.util.Set<String> systemFunctionNames) throws ProgramValidationException {

        if (jaxbFunction.getSInstructions() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SInstruction instruction :
                    jaxbFunction.getSInstructions().getSInstruction()) {
                validateInstructionFunctionReferences(instruction, jaxbFunctionMap, systemFunctionNames,
                        "function '" + jaxbFunction.getName() + "'");
            }
        }
    }

    private static void validateInstructionFunctionReferences(
            jaxb.engine.src.jaxb.schema.generated.SInstruction instruction,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            java.util.Set<String> systemFunctionNames,
            String context) throws ProgramValidationException {

        if (instruction.getSInstructionArguments() == null) {
            return;
        }

        String instructionName = instruction.getName();

        if ("JUMP_EQUAL_FUNCTION".equals(instructionName) || "QUOTE".equals(instructionName)) {
            String functionName = getArgumentValue(instruction.getSInstructionArguments(), "functionName");
            String programName = getArgumentValue(instruction.getSInstructionArguments(), "programName");
            String referencedFunction = functionName != null ? functionName : programName;

            if (referencedFunction != null) {
                boolean isDefined = jaxbFunctionMap.containsKey(referencedFunction) ||
                        (systemFunctionNames != null && systemFunctionNames.contains(referencedFunction));

                if (!isDefined) {
                    throw new ProgramValidationException(
                            "In " + context + ", instruction " + instructionName +
                                    " references undefined function: '" + referencedFunction +
                                    "' (function names are case-sensitive). " +
                                    "Available in file: " + jaxbFunctionMap.keySet() +
                                    (systemFunctionNames != null ? ", Available in system: " + systemFunctionNames : "")
                    );
                }

                String functionArgumentsStr = getArgumentValue(instruction.getSInstructionArguments(), "functionArguments");
                if (functionArgumentsStr != null && !functionArgumentsStr.trim().isEmpty()) {
                    validateFunctionArgumentsString(functionArgumentsStr, jaxbFunctionMap, systemFunctionNames, context, referencedFunction);
                }
            }
        }
    }

    private static void validateFunctionArgumentsString(
            String argumentsStr,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            java.util.Set<String> systemFunctionNames,
            String context,
            String parentFunction) throws ProgramValidationException {

        List<String> tokens = splitByCommaRespectingParentheses(argumentsStr);

        for (String token : tokens) {
            token = token.trim();

            if (token.startsWith("(") && token.endsWith(")")) {
                String innerContent = token.substring(1, token.length() - 1);
                int firstComma = innerContent.indexOf(',');

                String funcName;
                String funcArgs;

                if (firstComma == -1) {
                    funcName = innerContent.trim();
                    funcArgs = null;
                } else {
                    funcName = innerContent.substring(0, firstComma).trim();
                    funcArgs = innerContent.substring(firstComma + 1).trim();
                }

                boolean isDefined = jaxbFunctionMap.containsKey(funcName) ||
                        (systemFunctionNames != null && systemFunctionNames.contains(funcName));

                if (!isDefined) {
                    throw new ProgramValidationException(
                            "In " + context + ", function '" + parentFunction +
                                    "' has an argument that references undefined function: '" + funcName +
                                    "' (function names are case-sensitive). " +
                                    "Available in file: " + jaxbFunctionMap.keySet() +
                                    (systemFunctionNames != null ? ", Available in system: " + systemFunctionNames : "")
                    );
                }

                if (funcArgs != null && !funcArgs.trim().isEmpty()) {
                    validateFunctionArgumentsString(funcArgs, jaxbFunctionMap, systemFunctionNames, context, funcName);
                }
            }
        }
    }

    public static SProgram convertJAXBToEngine(jaxb.engine.src.jaxb.schema.generated.SProgram jaxbProgram) throws ProgramValidationException {
        return convertJAXBToEngine(jaxbProgram, null);
    }

    public static SProgram convertJAXBToEngine(jaxb.engine.src.jaxb.schema.generated.SProgram jaxbProgram,
                                               java.util.Set<String> systemFunctionNames) throws ProgramValidationException {
        if (jaxbProgram == null) {
            throw new ProgramValidationException("JAXB program cannot be null");
        }

        // Build a map of JAXB functions
        Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap = new HashMap<>();
        if (jaxbProgram.getSFunctions() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction : jaxbProgram.getSFunctions().getSFunction()) {
                jaxbFunctionMap.put(jaxbFunction.getName(), jaxbFunction);
            }
        }

        // Validate function references in all functions
        if (jaxbProgram.getSFunctions() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction : jaxbProgram.getSFunctions().getSFunction()) {
                validateFunctionReferences(jaxbFunction, jaxbFunctionMap, systemFunctionNames);
            }
        }

        // ===== CRITICAL FIX: Convert ALL functions FIRST and store in a map =====
        // This ensures there's only ONE instance of each SFunction
        Map<String, SFunction> convertedFunctions = new HashMap<>();
        if (jaxbProgram.getSFunctions() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction : jaxbProgram.getSFunctions().getSFunction()) {
                // Convert function WITHOUT its FunctionArgument dependencies first (creates empty instructions)
                SFunction engineFunction = convertFunctionShell(jaxbFunction);
                convertedFunctions.put(engineFunction.getName(), engineFunction);
            }

            // Now populate the instructions with access to all converted functions
            for (jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction : jaxbProgram.getSFunctions().getSFunction()) {
                SFunction engineFunction = convertedFunctions.get(jaxbFunction.getName());
                populateFunctionInstructions(jaxbFunction, engineFunction, jaxbFunctionMap, convertedFunctions, systemFunctionNames);
            }
        }

        // Convert main program instructions (now with access to all converted functions)
        List<SInstruction> instructionList = new ArrayList<>();
        if (jaxbProgram.getSInstructions() != null) {
            List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions = jaxbProgram.getSInstructions()
                    .getSInstruction();

            java.util.Set<String> definedLabels = collectDefinedLabels(jaxbInstructions);

            for (jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction : jaxbInstructions) {
                validateInstructionLabelReferences(jaxbInstruction, definedLabels);

                SInstruction engineInstruction = convertInstruction(jaxbInstruction, jaxbFunctionMap, convertedFunctions, systemFunctionNames);
                if (engineInstruction != null) {
                    instructionList.add(engineInstruction);
                }
            }
        }

        // Create the main program
        SProgramImpl engineProgram = new SProgramImpl(jaxbProgram.getName(), null, instructionList);

        // Store the converted functions in the program
        engineProgram.setLocalFunctions(convertedFunctions);

        return engineProgram;
    }

    // Create an empty SFunction shell without instructions
    private static SFunction convertFunctionShell(jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction) {
        return new SFunction(jaxbFunction.getName(), jaxbFunction.getUserString(), null, new ArrayList<>());
    }

    // Populate the function's instructions after all functions exist
    private static void populateFunctionInstructions(
            jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction,
            SFunction engineFunction,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            Map<String, SFunction> convertedFunctions,
            java.util.Set<String> systemFunctionNames) throws ProgramValidationException {

        List<SInstruction> instructionList = new ArrayList<>();
        if (jaxbFunction.getSInstructions() != null) {
            List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions =
                    jaxbFunction.getSInstructions().getSInstruction();

            java.util.Set<String> definedLabels = collectDefinedLabels(jaxbInstructions);

            for (jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction : jaxbInstructions) {
                validateInstructionLabelReferences(jaxbInstruction, definedLabels);

                SInstruction engineInstruction = convertInstruction(jaxbInstruction, jaxbFunctionMap,
                        convertedFunctions, systemFunctionNames);
                if (engineInstruction != null) {
                    instructionList.add(engineInstruction);
                }
            }
        }

        // Use reflection or a setter to add instructions to the existing function
        // Since SFunction extends SProgramImpl, we need to access the instruction list
        try {
            java.lang.reflect.Field instructionListField = SProgramImpl.class.getDeclaredField("instructionList");
            instructionListField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<SInstruction> existingList = (List<SInstruction>) instructionListField.get(engineFunction);
            existingList.addAll(instructionList);
        } catch (Exception e) {
            throw new ProgramValidationException("Failed to populate function instructions: " + e.getMessage(), e);
        }
    }

    private static SInstruction convertInstruction(
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            Map<String, SFunction> convertedFunctions,
            java.util.Set<String> systemFunctionNames) throws ProgramValidationException {
        if (jaxbInstruction == null) {
            return null;
        }

        String instructionName = jaxbInstruction.getName();
        String variableName = jaxbInstruction.getSVariable();

        Variable variable = createVariable(variableName);

        Label label = null;
        if (jaxbInstruction.getSLabel() != null && !jaxbInstruction.getSLabel().isEmpty()) {
            label = new LabelImpl(jaxbInstruction.getSLabel());
        }

        switch (instructionName) {
            case "INCREASE":
                return label != null ? new IncreaseInstruction(variable, label) : new IncreaseInstruction(variable);

            case "DECREASE":
                return label != null ? new DecreaseInstruction(variable, label) : new DecreaseInstruction(variable);

            case "ZERO_VARIABLE":
                return label != null ? new ZeroVariableInstruction(variable, label)
                        : new ZeroVariableInstruction(variable);

            case "GOTO_LABEL":
                return createGotoLabel(jaxbInstruction, label);

            case "JUMP_NOT_ZERO":
                return createJumpNotZero(variable, jaxbInstruction, label);

            case "JUMP_ZERO":
                return createJumpZero(variable, jaxbInstruction, label);

            case "ASSIGNMENT":
                return createAssignmentInstruction(variable, jaxbInstruction, label);

            case "CONSTANT_ASSIGNMENT":
                return createConstantAssignmentInstruction(variable, jaxbInstruction, label);

            case "JUMP_EQUAL_CONSTANT":
                return createJumpEqualConstant(variable, jaxbInstruction, label);

            case "JUMP_EQUAL_VARIABLE":
                return createJumpEqualVariable(variable, jaxbInstruction, label);

            case "JUMP_EQUAL_FUNCTION":
                return createJumpEqualFunction(variable, jaxbInstruction, label, jaxbFunctionMap, convertedFunctions, systemFunctionNames);

            case "QUOTE":
                return createQuoteInstruction(variable, jaxbInstruction, label, jaxbFunctionMap, convertedFunctions, systemFunctionNames);

            case "NEUTRAL":
                return label != null ? new NoOpInstruction(variable, label) : new NoOpInstruction(variable);

            default:
                System.err.println("Unknown instruction: " + instructionName);
                return new NoOpInstruction(variable);
        }
    }

    private static SInstruction createJumpEqualFunction(
            Variable variable,
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
            Label label,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            Map<String, SFunction> convertedFunctions,
            java.util.Set<String> systemFunctionNames) throws ProgramValidationException {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String functionName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "functionName");
            String targetLabelName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "JEFunctionLabel");
            String functionArgumentsStr = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "functionArguments");

            if (functionName != null) {
                FunctionArgument functionArgument = createFunctionArgument(
                        functionName, functionArgumentsStr, jaxbFunctionMap, convertedFunctions, systemFunctionNames);

                Label targetLabel;
                if (targetLabelName != null) {
                    targetLabel = new LabelImpl(targetLabelName);
                } else {
                    targetLabel = new LabelImpl(functionName);
                }

                if (label != null) {
                    return new JumpEqualFunction(variable, label, targetLabel, functionArgument);
                } else {
                    return new JumpEqualFunction(variable, targetLabel, functionArgument);
                }
            }
        }
        throw new IllegalArgumentException("JUMP_EQUAL_FUNCTION instruction requires a functionName argument");
    }

    private static FunctionArgument createFunctionArgument(
            String functionName,
            String functionArgumentsStr,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            Map<String, SFunction> convertedFunctions,
            java.util.Set<String> systemFunctionNames) throws ProgramValidationException {

        List<Argument> arguments = new ArrayList<>();
        if (functionArgumentsStr != null && !functionArgumentsStr.trim().isEmpty()) {
            arguments = parseArguments(functionArgumentsStr, jaxbFunctionMap, convertedFunctions, systemFunctionNames);
        }

        // CRITICAL: Check if this function was converted in THIS file (reuse the singleton instance)
        if (convertedFunctions.containsKey(functionName)) {
            SFunction engineFunction = convertedFunctions.get(functionName);
            return new FunctionArgument(engineFunction, arguments);
        }
        // Otherwise, it's a system function (will be resolved later by Engine)
        else {
            return new FunctionArgument(functionName, arguments);
        }
    }

    private static List<Argument> parseArguments(
            String argumentsStr,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            Map<String, SFunction> convertedFunctions,
            java.util.Set<String> systemFunctionNames) throws ProgramValidationException {
        List<Argument> arguments = new ArrayList<>();

        List<String> tokens = splitByCommaRespectingParentheses(argumentsStr);

        for (String token : tokens) {
            token = token.trim();

            if (token.startsWith("(") && token.endsWith(")")) {
                String innerContent = token.substring(1, token.length() - 1);

                int firstComma = innerContent.indexOf(',');

                String funcName;
                String funcArgs;

                if (firstComma == -1) {
                    funcName = innerContent.trim();
                    funcArgs = null;
                } else {
                    funcName = innerContent.substring(0, firstComma).trim();
                    funcArgs = innerContent.substring(firstComma + 1).trim();
                }

                FunctionArgument funcArg = createFunctionArgument(
                        funcName, funcArgs, jaxbFunctionMap, convertedFunctions, systemFunctionNames);
                arguments.add(funcArg);

            } else {
                Variable variable = createVariable(token);
                arguments.add(variable);
            }
        }

        return arguments;
    }

    private static List<String> splitByCommaRespectingParentheses(String str) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '(') {
                depth++;
                current.append(c);
            } else if (c == ')') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                if (current.length() > 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }

    private static SInstruction createQuoteInstruction(
            Variable variable,
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
            Label label,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            Map<String, SFunction> convertedFunctions,
            java.util.Set<String> systemFunctionNames) throws ProgramValidationException {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String programName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "programName");
            String functionName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "functionName");

            String quotedName = programName != null ? programName : functionName;
            String functionArgumentsStr = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "functionArguments");

            if (quotedName != null) {
                FunctionArgument functionArgument = createFunctionArgument(
                        quotedName, functionArgumentsStr, jaxbFunctionMap, convertedFunctions, systemFunctionNames);

                if (label != null) {
                    return new QuoteProgramInstruction(variable, label, functionArgument);
                } else {
                    return new QuoteProgramInstruction(variable, functionArgument);
                }
            }
        }

        return label != null ? new NoOpInstruction(variable, label) : new NoOpInstruction(variable);
    }

    private static Variable createVariable(String variableName) {
        if (variableName == null || variableName.isEmpty()) {
            return new VariableImpl(VariableType.RESULT, 0);
        }

        if (variableName.equals("y")) {
            return new VariableImpl(VariableType.RESULT, 0);
        } else if (variableName.startsWith("x")) {
            try {
                String numberStr = variableName.substring(1);
                int varNumber = Integer.parseInt(numberStr);
                return new VariableImpl(VariableType.INPUT, varNumber);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid input variable format: " + variableName +
                        ". Expected format: x<number> (e.g., x1, x2)");
            }
        } else if (variableName.startsWith("z")) {
            try {
                String numberStr = variableName.substring(1);
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
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String secondaryVarName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "assignedVariable");
            if (secondaryVarName != null) {
                return new AssignmentInstruction(variable, createVariable(secondaryVarName), label);
            }
        }
        throw new IllegalArgumentException("Assignment instruction requires an assignedVariable argument");
    }

    private static SInstruction createConstantAssignmentInstruction(Variable variable,
                                                                    jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String constantValue = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "constantValue");
            if (constantValue != null) {
                return new ConstantAssignmentInstruction(Long.parseLong(constantValue), variable, label);
            }
        }
        throw new IllegalArgumentException("Constant Assignment instruction requires a constantValue argument");
    }

    private static SInstruction createJumpEqualConstant(Variable variable,
                                                        jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String targetLabelName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "JEConstantLabel");
            String constantValue = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "constantValue");
            if (targetLabelName != null && constantValue != null) {
                Label targetLabel = new LabelImpl(targetLabelName);
                return label != null ? new JumpEqualConstant(variable, Long.parseLong(constantValue), label, targetLabel)
                        : new JumpEqualConstant(variable, Long.parseLong(constantValue), targetLabel);
            }
        }
        throw new IllegalArgumentException("JumpEqualConstant instruction requires both JEConstantLabel and constantValue arguments");
    }

    private static SInstruction createJumpEqualVariable(Variable variable,
                                                        jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String secondaryVarName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "variableName");
            String targetLabelName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "JEVariableLabel");
            if (secondaryVarName != null && targetLabelName != null) {
                Variable secondaryVariable = createVariable(secondaryVarName);
                Label targetLabel = new LabelImpl(targetLabelName);
                return label != null ? new JumpEqualVariable(variable, secondaryVariable, targetLabel, label)
                        : new JumpEqualVariable(variable, secondaryVariable, targetLabel);
            }
        }
        throw new IllegalArgumentException("JumpEqualVariable instruction requires both variableName and JEVariableLabel arguments");
    }

    private static SInstruction createGotoLabel(
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String targetLabelName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "gotoLabel");
            if (targetLabelName != null) {
                Label targetLabel = new LabelImpl(targetLabelName);
                return label != null ? new GotoLabel(label, targetLabel) : new GotoLabel(targetLabel);
            }
        }
        throw new IllegalArgumentException("GOTO_LABEL instruction requires a gotoLabel argument");
    }

    private static SInstruction createJumpNotZero(Variable variable,
                                                  jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String targetLabelName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "JNZLabel");
            if (targetLabelName != null) {
                Label targetLabel = new LabelImpl(targetLabelName);
                return label != null ? new JumpNotZeroInstruction(variable, label, targetLabel)
                        : new JumpNotZeroInstruction(variable, targetLabel);
            }
        }
        throw new IllegalArgumentException("JUMP_NOT_ZERO instruction requires a JNZLabel argument");
    }

    private static SInstruction createJumpZero(Variable variable,
                                               jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction, Label label) {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String targetLabelName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "JZLabel");
            if (targetLabelName != null) {
                Label targetLabel = new LabelImpl(targetLabelName);
                return label != null ? new JumpZero(variable, label, targetLabel)
                        : new JumpZero(variable, targetLabel);
            }
        }
        throw new IllegalArgumentException("JUMP_ZERO instruction requires a JZLabel argument");
    }

    private static String getArgumentValue(SInstructionArguments args, String name) {
        for (SInstructionArgument arg : args.getSInstructionArgument()) {
            if (name.equals(arg.getName())) {
                return arg.getValue();
            }
        }
        return null;
    }
}