package exception;

public class DegreeOutOfRangeException extends Exception {

    private final int minDegree;
    private final int maxDegree;

    public DegreeOutOfRangeException(int minDegree, int maxDegree) {
        super("Degree out of range. Valid range is from " + minDegree + " to " + maxDegree + ".");
        this.minDegree = minDegree;
        this.maxDegree = maxDegree;
    }

    public int getMaxDegree() {
        return maxDegree;
    }

    public int getMinDegree() {
        return minDegree;
    }
}

