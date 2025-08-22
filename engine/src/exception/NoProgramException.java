package exception;

public class NoProgramException extends Exception {

    private static final String MESSAGE = "No program loaded.";

    public NoProgramException() {
        super(MESSAGE);
    }

    public String getMessage() {
        return MESSAGE;
    }
}
