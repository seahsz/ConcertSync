package vttp.server.exceptions;

// Authentication/Authorization
public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException() {
        super("User is not authenticated");
    }
}
