package vttp.server.controllers;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vttp.server.exceptions.UsernameTakenException;
import vttp.server.exceptions.InvalidCredentialsException;
import vttp.server.exceptions.InvalidTokenException;
import vttp.server.exceptions.EmailNotFoundException;
import vttp.server.exceptions.EmailUnverifiedException;
import vttp.server.models.angularDto.ErrorResponse;
import vttp.server.models.angularDto.LoginRequest;
import vttp.server.models.angularDto.RegisterRequest;
import vttp.server.models.angularDto.SuccessResponse;
import vttp.server.services.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private Logger logger = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private AuthService authSvc;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            logger.info(">>> Attempting to register user");
            logger.info(request.toString());
            authSvc.register(request);
            return ResponseEntity
                    .ok(new SuccessResponse("Registration successful. Please verify your email").toJson().toString());
        } catch (EmailUnverifiedException ex) {
            ErrorResponse error = new ErrorResponse();
            error.getErrors().put("email_taken", true);
            return ResponseEntity.status(409).body(error.toJson().toString());
        } catch (UsernameTakenException ex) {
            ErrorResponse error = new ErrorResponse();
            error.getErrors().put("username_taken", true);
            return ResponseEntity.status(409).body(error.toJson().toString());
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            authSvc.verifyEmail(token);
            return ResponseEntity.ok(new SuccessResponse("Email verified successfully").toJson().toString());
        } catch (InvalidTokenException ex) {
            ErrorResponse error = new ErrorResponse();
            error.getErrors().put("invalid_token", true);
            return ResponseEntity.status(400).body(error.toJson().toString());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        try {
            String token = authSvc.login(request);
            return ResponseEntity.ok(new SuccessResponse("Login successful", token).toJson().toString());
        } catch (InvalidCredentialsException ex) {
            ErrorResponse error = new ErrorResponse();
            error.getErrors().put("invalid_credentials", true);
            error.getErrors().put("email_unverified", false);
            return ResponseEntity.status(401).body(error.toJson().toString());
        } catch (EmailUnverifiedException ex) {
            ErrorResponse error = new ErrorResponse();
            error.getErrors().put("invalid_credentials", false);
            error.getErrors().put("email_unverified", true);
            return ResponseEntity.status(401).body(error.toJson().toString());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestBody String username) {
        try {
            authSvc.resendVerificationEmail(username);
            return ResponseEntity.ok(new SuccessResponse("Verification email sent successfully").toJson().toString());
        } catch (EmailNotFoundException ex) {
            ErrorResponse error = new ErrorResponse();
            error.getErrors().put("user_not_found", true);
            return ResponseEntity.status(404).body(error.toJson().toString());
        } catch (Exception ex) {
            logger.warning("Error resending verification email: " + ex.getMessage());
            ErrorResponse error = new ErrorResponse();
            error.getErrors().put("unexpected", true);
            return ResponseEntity.status(500).body(error.toJson().toString());
        }
    }

}
