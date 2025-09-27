package core.logic.engine;

import core.logic.instruction.mostInstructions.SInstruction;
import core.logic.instruction.quoteInstructions.Argument;
import core.logic.instruction.quoteInstructions.FunctionArgument;
import core.logic.instruction.quoteInstructions.Quotable;
import core.logic.program.SProgram;

import java.util.*;

import static core.logic.instruction.quoteInstructions.Utils.getProgramsNames;

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
        List<SInstruction> instructions = program.getInstructionList();
        for(SInstruction instruction : instructions) {
            if(instruction instanceof Quotable quotable) {
                names.addAll(getProgramsNames(quotable.getFunctionArgument()));

            }
        }

        return names;
    }

    private Map<String, SProgram> calculateNameToProgram() {
        Map<String, SProgram> nameToProgram = new HashMap<>();
        nameToProgram.put(program.getName(), program);
        List<SInstruction> instructions = program.getInstructionList();
        for(SInstruction instruction : instructions) {
            if(instruction instanceof Quotable quotable) {
                getNamesToProgramsFromFunctionArgument(quotable.getFunctionArgument(), nameToProgram);
            }
        }

        return nameToProgram;
    }

    private void getNamesToProgramsFromFunctionArgument(FunctionArgument functionArgument, Map<String, SProgram> nameToProgram) {
        SProgram program = functionArgument.getProgram();
        if(!nameToProgram.containsKey(program.getName())) {
            nameToProgram.put(program.getName(), program);
        }

        List<Argument> arguments = functionArgument.getArguments();
        for(Argument argument : arguments) {
            if(argument instanceof FunctionArgument funcArg) {
                getNamesToProgramsFromFunctionArgument(funcArg, nameToProgram);
            }
        }
    }
}
