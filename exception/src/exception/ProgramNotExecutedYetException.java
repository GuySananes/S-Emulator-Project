package exception;

public class ProgramNotExecutedYetException extends Exception {
    public ProgramNotExecutedYetException(String progName) {
        super("Program" + progName +"has not been executed yet.");
    }
}
