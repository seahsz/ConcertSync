package vttp.server.exceptions;

public class InvalidConcertDateException extends RuntimeException {
    public InvalidConcertDateException(String message) {
        super(message);
    }
}
