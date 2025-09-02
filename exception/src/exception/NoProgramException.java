package exception;

public class NoProgramException extends Exception {

    public NoProgramException() {
        super("No program loaded. Please load a program first.");
    }
}
