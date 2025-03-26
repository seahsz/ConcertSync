package vttp.server.exceptions;

public class GroupLimitExceededException extends RuntimeException {
    public GroupLimitExceededException(String message) {
        super(message);
    }
}
