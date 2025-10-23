package sserver.registry.model;

import java.util.concurrent.atomic.AtomicInteger;

public class UserStats {
    private final String name;
    private final AtomicInteger credits = new AtomicInteger(0);
    private final AtomicInteger used = new AtomicInteger(0);
    private final AtomicInteger runs = new AtomicInteger(0);
    private final AtomicInteger mainCount = new AtomicInteger(0);
    private final AtomicInteger funcCount = new AtomicInteger(0);

    public UserStats(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public int getCredits() { return credits.get(); }
    public int getUsed() { return used.get(); }
    public int getRuns() { return runs.get(); }
    public int getMainCount() { return mainCount.get(); }
    public int getFuncCount() { return funcCount.get(); }

    public void addCredits(int amount) {
        credits.addAndGet(amount);
    }

    public void addUsed(int amount) {
        used.addAndGet(amount);
    }

    public void incrementRuns() {
        runs.incrementAndGet();
    }

    public void incrementMainCount() {
        mainCount.incrementAndGet();
    }

    public void incrementFuncCount() {
        funcCount.incrementAndGet();
    }
}