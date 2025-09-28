package exception;

public class NoSuchRunException extends Exception {
    public NoSuchRunException() {
        super("No such run exists.");
    }
}
