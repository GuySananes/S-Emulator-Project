package present.program;

public class FunctionSummary {
    public final String id;
    public final String functionName;
    public final String parentProgramName;
    public final String owner;
    public final int instructionCount;
    public final int maxDegree;

    public FunctionSummary(String id, String functionName, String parentProgramName,
                           String owner, int instructionCount, int maxDegree) {
        this.id = id;
        this.functionName = functionName;
        this.parentProgramName = parentProgramName;
        this.owner = owner;
        this.instructionCount = instructionCount;
        this.maxDegree = maxDegree;
    }

    public String getId() { return id; }
    public String getFunctionName() { return functionName; }
    public String getParentProgramName() { return parentProgramName; }
    public String getOwner() { return owner; }
    public int getInstructionCount() { return instructionCount; }
    public int getMaxDegree() { return maxDegree; }
}