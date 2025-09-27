package exception;

public class NoSuchProgramInContextException extends Exception {
    public NoSuchProgramInContextException() {
        super("no such program in context");
    }
}
