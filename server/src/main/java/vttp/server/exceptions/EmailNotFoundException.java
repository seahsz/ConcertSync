package vttp.server.exceptions;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException() {
        super("Email/Username not found");
    }
}