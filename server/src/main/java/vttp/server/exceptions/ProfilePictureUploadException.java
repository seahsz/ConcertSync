package vttp.server.exceptions;

public class ProfilePictureUploadException extends RuntimeException {
    public ProfilePictureUploadException(long id, String errorMsg) {
        super("Error uploading profile picture for id %d: %s".formatted(id, errorMsg));
    }
}
