package vttp.server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vttp.server.models.angularDto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(EmailTakenException.class)
    public ResponseEntity<String> handleEmailTakenException(EmailTakenException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("email_taken", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<String> handleUsernameTakenException(UsernameTakenException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("username_taken", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("invalid_credentials", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailUnverifiedException.class)
    public ResponseEntity<String> handleEmailUnverifiedException(EmailUnverifiedException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("email_unverified", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    public ResponseEntity<String> handleNotAuthenticatedException(NotAuthenticatedException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("not_authenticated", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotPremiumException.class)
    public ResponseEntity<String> handleNotPremiumException(NotPremiumException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("not_premium", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("invalid_token", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NameUpdateFrequencyException.class)
    public ResponseEntity<String> handleNameUpdateFrequencyException(NameUpdateFrequencyException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("profile_picture_upload_failed", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProfilePictureUploadException.class)
    public ResponseEntity<String> handleProfilePictureUploadException(ProfilePictureUploadException ex) {
        ErrorResponse error = new ErrorResponse();
        error.getErrors().put("profile_picture_upload_failed", true);
        return new ResponseEntity<>(error.toJson().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
}
