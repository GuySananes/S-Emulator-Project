package exception;

public class DegreeOutOfRangeException extends Exception {


    private final String MESSAGE;
    private final int minDegree;
    private final int maxDegree;

    public DegreeOutOfRangeException(int minDegree, int maxDegree) {
        super();
        this.MESSAGE = "Degree out of range. Valid range is from " + minDegree + " to " + maxDegree + ".";
        this.minDegree = minDegree;
        this.maxDegree = maxDegree;
    }

    public String getMessage() {
        return MESSAGE;
    }

    public int getMinDegree() {
        return minDegree;
    }

    public int getMaxDegree() {
        return maxDegree;
    }
}

