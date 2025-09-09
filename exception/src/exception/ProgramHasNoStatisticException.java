package exception;

public class ProgramHasNoStatisticException extends Exception{
    public ProgramHasNoStatisticException(){
        super("Program has no statistic yet.");
    }
}
