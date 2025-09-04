package exception;

public class ProgramValidationException extends Exception {
    public ProgramValidationException(String message) {
        super(message);
    }
    public ProgramValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
