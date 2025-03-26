package vttp.server.exceptions;

// Registration
public class EmailTakenException extends RuntimeException {
    public EmailTakenException() { super("Email is already taken"); }
}
