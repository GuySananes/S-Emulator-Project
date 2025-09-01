package exception;

public class XMLUnmarshalException extends Exception {

    public XMLUnmarshalException(String message) {
        super(message);
    }

    public XMLUnmarshalException(String message, Throwable cause) {
        super(message, cause);
    }
}