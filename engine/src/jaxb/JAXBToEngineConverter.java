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
     * @throws ProgramValidationException if a referenced label is not found or missing
     */
    private static void validateInstructionLabelReferences(
            jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
            java.util.Set<String> definedLabels) throws ProgramValidationException {

        String instructionName = jaxbInstruction.getName();
        String referencedLabel = getReferencedLabel(jaxbInstruction);

        // Instructions that use labels as jump targets
        if (isLabelReferencingInstruction(instructionName)) {
            if (referencedLabel == null || referencedLabel.isEmpty()) {
                throw new ProgramValidationException(
                    "Instruction " + instructionName + " requires a label but none was provided"
                );
            }

            // Allow special system labels like "EXIT"
            if (isSystemLabel(referencedLabel)) {
                return; // System labels don't need to be defined in the program
            }

            if (!definedLabels.contains(referencedLabel)) {
                throw new ProgramValidationException(
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
                        "JZLabel".equals(arg.getName()) || "jumpLabel".equals(arg.getName()) ||
                        "JEConstantLabel".equals(arg.getName()) || "JEVariableLabel".equals(arg.getName()) ||
                        "JEFunctionLabel".equals(arg.getName())) {  // Remove functionName, only JEFunctionLabel is the actual jump target
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
            case "JUMP_EQUAL_FUNCTION":
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


    /**
     * Validates that all function references in the main program are defined.
     * Checks both direct function calls and nested function arguments.
     *
     * @param jaxbInstructions List of JAXB instructions from main program
     * @param jaxbFunctionMap Map of all defined functions in the file
     * @throws ProgramValidationException if a referenced function is not defined
     */
    private static void validateMainProgramFunctionReferences(
            List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap) throws ProgramValidationException {

        for (jaxb.engine.src.jaxb.schema.generated.SInstruction instruction : jaxbInstructions) {
            validateInstructionFunctionReferences(instruction, jaxbFunctionMap, "main program");
        }
    }

    /**
     * Validates that all function references within a function are defined.
     *
     * @param jaxbFunction The function to validate
     * @param jaxbFunctionMap Map of all defined functions in the file
     * @throws ProgramValidationException if a referenced function is not defined
     */
    private static void validateFunctionReferences(
            jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap) throws ProgramValidationException {

        if (jaxbFunction.getSInstructions() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SInstruction instruction :
                    jaxbFunction.getSInstructions().getSInstruction()) {
                validateInstructionFunctionReferences(instruction, jaxbFunctionMap,
                        "function '" + jaxbFunction.getName() + "'");
            }
        }
    }

    /**
     * Validates function references within a single instruction.
     *
     * @param instruction The instruction to validate
     * @param jaxbFunctionMap Map of all defined functions
     * @param context Description of where the instruction appears (for error messages)
     * @throws ProgramValidationException if a referenced function is not defined
     */
    private static void validateInstructionFunctionReferences(
            jaxb.engine.src.jaxb.schema.generated.SInstruction instruction,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            String context) throws ProgramValidationException {

        if (instruction.getSInstructionArguments() == null) {
            return;
        }

        String instructionName = instruction.getName();

        // Check JUMP_EQUAL_FUNCTION and QUOTE instructions
        if ("JUMP_EQUAL_FUNCTION".equals(instructionName) || "QUOTE".equals(instructionName)) {
            String functionName = getArgumentValue(instruction.getSInstructionArguments(), "functionName");
            String programName = getArgumentValue(instruction.getSInstructionArguments(), "programName");
            String referencedFunction = functionName != null ? functionName : programName;

            if (referencedFunction != null) {
                // Validate the main function reference (case-sensitive)
                if (!jaxbFunctionMap.containsKey(referencedFunction)) {
                    throw new ProgramValidationException(
                            "In " + context + ", instruction " + instructionName +
                                    " references undefined function: '" + referencedFunction +
                                    "' (function names are case-sensitive). Available functions: " +
                                    jaxbFunctionMap.keySet()
                    );
                }

                // Validate nested function arguments
                String functionArgumentsStr = getArgumentValue(instruction.getSInstructionArguments(), "functionArguments");
                if (functionArgumentsStr != null && !functionArgumentsStr.trim().isEmpty()) {
                    validateFunctionArgumentsString(functionArgumentsStr, jaxbFunctionMap, context, referencedFunction);
                }
            }
        }
    }

    /**
     * Validates all function references within a function arguments string.
     * Recursively checks nested function arguments.
     *
     * @param argumentsStr The arguments string to validate
     * @param jaxbFunctionMap Map of all defined functions
     * @param context Description of where the arguments appear (for error messages)
     * @param parentFunction Name of the parent function being called
     * @throws ProgramValidationException if a referenced function is not defined
     */
    private static void validateFunctionArgumentsString(
            String argumentsStr,
            Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap,
            String context,
            String parentFunction) throws ProgramValidationException {

        List<String> tokens = splitByCommaRespectingParentheses(argumentsStr);

        for (String token : tokens) {
            token = token.trim();

            if (token.startsWith("(") && token.endsWith(")")) {
                // It's a function argument
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

                // Validate this function exists (case-sensitive)
                if (!jaxbFunctionMap.containsKey(funcName)) {
                    throw new ProgramValidationException(
                            "In " + context + ", function '" + parentFunction +
                                    "' has an argument that references undefined function: '" + funcName +
                                    "' (function names are case-sensitive). Available functions: " +
                                    jaxbFunctionMap.keySet()
                    );
                }

                // Recursively validate nested arguments
                if (funcArgs != null && !funcArgs.trim().isEmpty()) {
                    validateFunctionArgumentsString(funcArgs, jaxbFunctionMap, context, funcName);
                }
            }
            // Variables don't need validation for function existence
        }
    }




    public static SProgram convertJAXBToEngine(jaxb.engine.src.jaxb.schema.generated.SProgram jaxbProgram) throws ProgramValidationException {
        if (jaxbProgram == null) {
            throw new ProgramValidationException("JAXB program cannot be null");
        }

        // First, build a map of JAXB functions for later reference when creating Quote/JumpEqualFunction instructions
        Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap = new HashMap<>();
        if (jaxbProgram.getSFunctions() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction : jaxbProgram.getSFunctions().getSFunction()) {
                jaxbFunctionMap.put(jaxbFunction.getName(), jaxbFunction);
            }
        }

        // Validate function references in all functions
        if (jaxbProgram.getSFunctions() != null) {
            for (jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction : jaxbProgram.getSFunctions().getSFunction()) {
                validateFunctionReferences(jaxbFunction, jaxbFunctionMap);
            }
        }

        // Convert instructions (with access to the JAXB function map)
        List<SInstruction> instructionList = new ArrayList<>();
        if (jaxbProgram.getSInstructions() != null) {
            List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions = jaxbProgram.getSInstructions()
                    .getSInstruction();

            // Collect all defined labels first for validation
            java.util.Set<String> definedLabels = collectDefinedLabels(jaxbInstructions);

            for (jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction : jaxbInstructions) {
                // Validate each instruction's label references
                validateInstructionLabelReferences(jaxbInstruction, definedLabels);

                SInstruction engineInstruction = convertInstruction(jaxbInstruction, jaxbFunctionMap);
                if (engineInstruction != null) {
                    instructionList.add(engineInstruction);
                }
            }
        }

        // Create the real engine program with all instructions
        SProgram engineProgram = new SProgramImpl(jaxbProgram.getName(), null, instructionList);

        return engineProgram;
    }

    private static SInstruction convertInstruction(jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
                                                   Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap) throws ProgramValidationException {
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
            // Use the string constructor that handles 'L' prefix and special cases like "EXIT"
            label = new LabelImpl(jaxbInstruction.getSLabel());
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
                return createJumpEqualFunction(variable, jaxbInstruction, label, jaxbFunctionMap);

            case "QUOTE":
                return createQuoteInstruction(variable, jaxbInstruction, label, jaxbFunctionMap);

            case "NEUTRAL":
                return label != null ? new NoOpInstruction(variable, label) : new NoOpInstruction(variable);

            default:
                System.err.println("Unknown instruction: " + instructionName);
                return new NoOpInstruction(variable);
        }
    }


    private static SInstruction createJumpEqualFunction(Variable variable,
                                                        jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
                                                        Label label,
                                                        Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap) throws ProgramValidationException {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            String functionName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "functionName");
            String targetLabelName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "JEFunctionLabel");
            String functionArgumentsStr = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "functionArguments");

            if (functionName != null) {
                // Create the FunctionArgument - THIS IS WHERE THE FUNCTION IS CREATED
                FunctionArgument functionArgument = createFunctionArgument(functionName, functionArgumentsStr, jaxbFunctionMap);

                // Determine target label
                Label targetLabel;
                if (targetLabelName != null) {
                    targetLabel = new LabelImpl(targetLabelName);
                } else {
                    // Use function name as target label if no explicit target provided
                    targetLabel = new LabelImpl(functionName);
                }

                // Use the correct constructor
                if (label != null) {
                    return new JumpEqualFunction(variable, label, targetLabel, functionArgument);
                } else {
                    return new JumpEqualFunction(variable, targetLabel, functionArgument);
                }
            }
        }
        throw new IllegalArgumentException("JUMP_EQUAL_FUNCTION instruction requires a functionName argument");
    }

    // Updated helper method to create FunctionArgument from JAXB function data
    private static FunctionArgument createFunctionArgument(String functionName,
                                                           String functionArgumentsStr,
                                                           Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap) throws ProgramValidationException {
        // Look up the JAXB function from the map
        jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction = jaxbFunctionMap.get(functionName);

        SFunction engineFunction;
        if (jaxbFunction != null) {
            // Convert the JAXB function to engine SFunction HERE - pass the function map
            engineFunction = convertFunction(jaxbFunction, jaxbFunctionMap);
        } else {
            // REPLACE THE FALLBACK WITH STRICT VALIDATION
            throw new ProgramValidationException(
                    "Function '" + functionName + "' is not defined in this file. " +
                            "Available functions: " + jaxbFunctionMap.keySet()
            );
        }

        // Parse function arguments - this also validates nested function references
        List<Argument> arguments = new ArrayList<>();
        if (functionArgumentsStr != null && !functionArgumentsStr.trim().isEmpty()) {
            arguments = parseArguments(functionArgumentsStr, jaxbFunctionMap);
        }

        // Create and return the FunctionArgument with the converted engine function
        return new FunctionArgument(engineFunction, arguments);
    }

    /**
     * Parses function arguments string into a list of Argument objects.
     * Format examples:
     * - "(Const7),(Successor,x1),x3" - mixed function and variable arguments
     * - "x1,x2" - only variables
     * - "(Func1),(Func2,x1)" - only functions
     *
     * @param argumentsStr The arguments string to parse
     * @param jaxbFunctionMap Map of available JAXB functions
     * @return List of parsed Argument objects (Variables or FunctionArguments)
     */
    private static List<Argument> parseArguments(String argumentsStr,
                                                 Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap) throws ProgramValidationException {
        List<Argument> arguments = new ArrayList<>();

        // Split by commas, but respect parentheses
        List<String> tokens = splitByCommaRespectingParentheses(argumentsStr);

        for (String token : tokens) {
            token = token.trim();

            if (token.startsWith("(") && token.endsWith(")")) {
                // It's a function argument: (FunctionName) or (FunctionName,arg1,arg2,...)
                String innerContent = token.substring(1, token.length() - 1);

                // Split the inner content by comma to get function name and its arguments
                int firstComma = innerContent.indexOf(',');

                String funcName;
                String funcArgs;

                if (firstComma == -1) {
                    // No arguments: (FunctionName)
                    funcName = innerContent.trim();
                    funcArgs = null;
                } else {
                    // Has arguments: (FunctionName,arg1,arg2,...)
                    funcName = innerContent.substring(0, firstComma).trim();
                    funcArgs = innerContent.substring(firstComma + 1).trim();
                }

                // Recursively create the FunctionArgument
                FunctionArgument funcArg = createFunctionArgument(funcName, funcArgs, jaxbFunctionMap);
                arguments.add(funcArg);

            } else {
                // It's a variable: x1, x2, y, z1, etc.
                Variable variable = createVariable(token);
                arguments.add(variable);
            }
        }

        return arguments;
    }

    /**
     * Splits a string by commas while respecting parentheses.
     * Example: "(Const7),(Successor,x1),x3" -> ["(Const7)", "(Successor,x1)", "x3"]
     *
     * @param str The string to split
     * @return List of tokens split by commas at depth 0
     */
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
                // Found a comma at depth 0 - this is a separator
                if (current.length() > 0) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }

        // Add the last token
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }


    // Convert JAXB SFunction to Engine SFunction
    private static SFunction convertFunction(jaxb.engine.src.jaxb.schema.generated.SFunction jaxbFunction,
                                             Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap) throws ProgramValidationException {
        if (jaxbFunction == null) {
            return null;
        }

        List<SInstruction> instructionList = new ArrayList<>();
        if (jaxbFunction.getSInstructions() != null) {
            List<jaxb.engine.src.jaxb.schema.generated.SInstruction> jaxbInstructions = jaxbFunction.getSInstructions().getSInstruction();

            // Collect all defined labels first for validation
            java.util.Set<String> definedLabels = collectDefinedLabels(jaxbInstructions);

            for (jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction : jaxbInstructions) {
                // Validate each instruction's label references
                validateInstructionLabelReferences(jaxbInstruction, definedLabels);

                SInstruction engineInstruction = convertInstruction(jaxbInstruction, jaxbFunctionMap);
                if (engineInstruction != null) {
                    instructionList.add(engineInstruction);
                }
            }
        }

        // Create engine function using the 4-parameter constructor (name, userName, originalProgram, instructions)
        SFunction engineFunction = new SFunction(jaxbFunction.getName(), jaxbFunction.getUserString(), null, instructionList);

        return engineFunction;
    }

    private static SInstruction createQuoteInstruction(Variable variable,
                                                       jaxb.engine.src.jaxb.schema.generated.SInstruction jaxbInstruction,
                                                       Label label,
                                                       Map<String, jaxb.engine.src.jaxb.schema.generated.SFunction> jaxbFunctionMap) throws ProgramValidationException {
        if (jaxbInstruction.getSInstructionArguments() != null) {
            // Look for program or function arguments
            String programName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "programName");
            String functionName = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "functionName");

            // Use whichever is available
            String quotedName = programName != null ? programName : functionName;
            String functionArgumentsStr = getArgumentValue(jaxbInstruction.getSInstructionArguments(), "functionArguments");


            if (quotedName != null) {
                // Create FunctionArgument - THIS IS WHERE THE FUNCTION IS CREATED
                FunctionArgument functionArgument = createFunctionArgument(quotedName, functionArgumentsStr, jaxbFunctionMap);

                // Create appropriate quote instruction
                if (label != null) {
                    return new QuoteProgramInstruction(variable, label, functionArgument);
                } else {
                    return new QuoteProgramInstruction(variable, functionArgument);
                }
            }
        }

        // If no proper quote arguments found, return NoOp as fallback
        return label != null ? new NoOpInstruction(variable, label) : new NoOpInstruction(variable);
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
