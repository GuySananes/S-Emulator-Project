
package present.program;

public class ProgramSummary {
    public final String id;
    public final String name;
    public final String owner;
    public final int instructionCount;  // Instructions at degree 0
    public final int maxDegree;
    public final int executionCount;
    public final double avgCreditCost;

    public ProgramSummary(String id, String name, String owner, int instructionCount,
                          int maxDegree, int executionCount, double avgCreditCost) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.instructionCount = instructionCount;
        this.maxDegree = maxDegree;
        this.executionCount = executionCount;
        this.avgCreditCost = avgCreditCost;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public int getInstructionCount() { return instructionCount; }
    public int getMaxDegree() { return maxDegree; }
    public int getExecutionCount() { return executionCount; }
    public double getAvgCreditCost() { return avgCreditCost; }
}