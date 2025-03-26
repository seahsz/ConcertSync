package vttp.server.exceptions;

// Registration
public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException() {
        super("Username is already taken");
    }
}
