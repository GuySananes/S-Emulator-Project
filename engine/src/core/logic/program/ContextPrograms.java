
package core.logic.program;

import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.instruction.quoteInstructions.FunctionArgument;
import core.logic.instruction.quoteInstructions.Quotable;

import java.util.*;

public class ContextPrograms {
    private final SProgram program;
    private Set<String> names = null;
    private Map<String, SProgram> nameToProgram = null;

    ContextPrograms(SProgram program) {
        this.program = program;
    }

    public Set<String> getNames() {
        if(names == null) {
            names = calculateNames();
        }

        return names;
    }

    public Map<String, SProgram> getNameToProgram() {
        if(nameToProgram == null) {
            nameToProgram = calculateNameToProgram();
        }

        return nameToProgram;
    }

    private Set<String> calculateNames() {
        Set<String> names = new LinkedHashSet<>();
        names.add(program.getName());

        // Add local functions first
        if (program instanceof SProgramImpl) {
            SProgramImpl programImpl = (SProgramImpl) program;
            names.addAll(programImpl.getLocalFunctions().keySet());
        }

        // Then add functions referenced in instructions
        List<SInstruction> instructions = program.getInstructionList();
        for(SInstruction instruction : instructions) {
            if(instruction instanceof Quotable quotable) {
                names.addAll(getProgramsNames(quotable.getFunctionArgument()));
            }
        }

        return names;
    }

    private Set<String> getProgramsNames(FunctionArgument functionArgument) {
        Set<String> names = new LinkedHashSet<>();
        names.add(functionArgument.getFunctionName());

        for (core.logic.instruction.quoteInstructions.Argument arg : functionArgument.getArguments()) {
            if (arg instanceof FunctionArgument) {
                names.addAll(getProgramsNames((FunctionArgument) arg));
            }
        }

        return names;
    }

    private Map<String, SProgram> calculateNameToProgram() {
        Map<String, SProgram> nameToProgram = new HashMap<>();
        nameToProgram.put(program.getName(), program);

        // Add local functions first
        if (program instanceof SProgramImpl) {
            SProgramImpl programImpl = (SProgramImpl) program;
            nameToProgram.putAll(programImpl.getLocalFunctions());
        }

        // MODIFIED: Only add functions from RESOLVED function arguments
        // Don't try to traverse unresolved system functions
        List<SInstruction> instructions = program.getInstructionList();
        for(SInstruction instruction : instructions) {
            if(instruction instanceof Quotable quotable) {
                updateNamesToProgramsFromFunctionArgument(quotable.getFunctionArgument(), nameToProgram);
            }
        }

        return nameToProgram;
    }

    private void updateNamesToProgramsFromFunctionArgument(FunctionArgument functionArgument, Map<String, SProgram> nameToProgram) {
        // MODIFIED: Skip unresolved system functions to avoid circular dependency
        if (functionArgument.isSystemFunction()) {
            // System function - it will be in localFunctions already if it exists
            // Don't try to call getProgram() on unresolved system functions
            return;
        }

        // Local function - safe to traverse
        SProgram program = functionArgument.getProgram();
        if(!nameToProgram.containsKey(program.getName())) {
            nameToProgram.put(program.getName(), program);
        }

        // Recursively traverse arguments
        List<core.logic.instruction.quoteInstructions.Argument> arguments = functionArgument.getArguments();
        for(core.logic.instruction.quoteInstructions.Argument argument : arguments) {
            if(argument instanceof FunctionArgument funcArg) {
                updateNamesToProgramsFromFunctionArgument(funcArg, nameToProgram);
            }
        }
    }
}