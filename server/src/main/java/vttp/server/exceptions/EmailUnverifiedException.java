package vttp.server.exceptions;

// Thrown in user has not verified his email prior to logging in
public class EmailUnverifiedException extends RuntimeException {
    public EmailUnverifiedException() {
        super("Email is not verified");
    }
}
