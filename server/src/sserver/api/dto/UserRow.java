package sserver.api.dto;

public class UserRow {
    public String name;
    public int mainCount;
    public int funcCount;
    public int credits;
    public int used;
    public int runs;

    public UserRow(String name, int mainCount, int funcCount, int credits, int used, int runs) {
        this.name = name;
        this.mainCount = mainCount;
        this.funcCount = funcCount;
        this.credits = credits;
        this.used = used;
        this.runs = runs;
    }

    // Add getters for JavaFX PropertyValueFactory
    public String getName() { return name; }
    public int getMainCount() { return mainCount; }
    public int getFuncCount() { return funcCount; }
    public int getCredits() { return credits; }
    public int getUsed() { return used; }
    public int getRuns() { return runs; }
}