package vttp.server.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long id) {
        super("User not found for id: %d".formatted(id));
    }
}
