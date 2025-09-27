package core.logic.execution;

import core.logic.program.SProgram;

import java.util.HashMap;
import java.util.Map;

public class RunCount {
    private static final Map<SProgram, Integer> runCounts = new HashMap<>();

    private RunCount(){}

    public static int getRunCount(SProgram program) {
        return runCounts.computeIfAbsent(program, p -> 0);
    }

    public static void incrementRunCount(SProgram program) {
        runCounts.put(program, getRunCount(program) + 1);
    }
}
