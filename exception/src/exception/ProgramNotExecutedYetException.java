package exception;

public class ProgramNotExecutedYetException extends Exception {
    public ProgramNotExecutedYetException() {
        super("Program has not been executed yet.");
    }
}
