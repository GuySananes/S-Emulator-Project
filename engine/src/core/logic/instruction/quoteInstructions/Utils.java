package core.logic.instruction.quoteInstructions;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Utils {
    public static Set<String> getProgramsNames(FunctionArgument functionArgument) {
        Set<String> names = new LinkedHashSet<>();
        names.add(functionArgument.getProgram().getName());
        List<Argument> arguments = functionArgument.getArguments();
        for(Argument argument : arguments) {
            if(argument instanceof FunctionArgument funcArg) {
                names.addAll(getProgramsNames(funcArg));
            }
        }

        return names;
    }
}
