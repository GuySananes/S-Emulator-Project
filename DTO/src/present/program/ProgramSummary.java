
package present.program;

public class ProgramSummary {
    public final String id;
    public final String name;
    public final String owner;
    public final int instructionCount;
    public final String grade;
    public final int executionCount;
    public final double avgExecutionTime;

    public ProgramSummary(String id, String name, String owner, int instructionCount,
                          String grade, int executionCount, double avgExecutionTime) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.instructionCount = instructionCount;
        this.grade = grade;
        this.executionCount = executionCount;
        this.avgExecutionTime = avgExecutionTime;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public int getInstructionCount() { return instructionCount; }
    public String getGrade() { return grade; }
    public int getExecutionCount() { return executionCount; }
    public double getAvgExecutionTime() { return avgExecutionTime; }
}