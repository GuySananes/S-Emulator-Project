package exception;

public class NoSuchRunException extends Exception {
    private final int startCount;
    private final int runCount;

    public NoSuchRunException(int startCount, int runCount) {
        super("No such run exists. Valid run numbers are from " + startCount + " to " + runCount + ".");
        this.startCount = startCount;
        this.runCount = runCount;
    }

    public int getStartCount() {
        return startCount;
    }

    public int getRunCount() {
        return runCount;
    }
}
