package vttp.server.exceptions;

public class NotPremiumException extends RuntimeException {
    public NotPremiumException() {
        super("Premium access required");
    }
}
