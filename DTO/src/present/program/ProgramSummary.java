package present.program;

public class ProgramSummary {
  public String name;
  public String owner;
  public int instructionCount;
  public String grade;
  public int runCount;
  public double avgCredits;
  public ProgramSummary() {}
  public ProgramSummary(String n,String o,int ic,String g,int rc,double ac){
    name=n; owner=o; instructionCount=ic; grade=g; runCount=rc; avgCredits=ac;
  }
}
