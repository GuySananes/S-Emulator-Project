package core.logic.execution;

import core.logic.program.SProgram;

import java.util.HashMap;
import java.util.Map;

public class RunCount {
    private static final Map<String, Integer> runCounts = new HashMap<>();

    private RunCount(){}

    public static int getRunCount(String progName) {
        return runCounts.computeIfAbsent(progName, p -> 0);
    }

    public static void incrementRunCount(String progName) {
        runCounts.put(progName, getRunCount(progName) + 1);
    }
}
