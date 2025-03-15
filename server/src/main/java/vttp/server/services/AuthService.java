package vttp.server.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import vttp.server.exceptions.UsernameTakenException;
import vttp.server.exceptions.InvalidCredentialsException;
import vttp.server.exceptions.InvalidTokenException;
import vttp.server.exceptions.EmailUnverifiedException;
import vttp.server.models.User;
import vttp.server.models.angularDto.LoginRequest;
import vttp.server.models.angularDto.RegisterRequest;
import vttp.server.repositories.UserRepository;
import vttp.server.utilities.jwt.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String BASE_URL = "http://localhost:4200/verify-email?token="; // TO CHANGE

    public void register(RegisterRequest rr) {
        if (userRepo.findByEmail(rr.getEmail()).isPresent()) {
            throw new EmailUnverifiedException();
        }
        if (userRepo.findByUsername(rr.getUsername()).isPresent()) {
            throw new UsernameTakenException();
        }

        User user = new User();
        user.setUsername(rr.getUsername());
        user.setEmail(rr.getEmail());
        user.setPassword(passwordEncoder.encode(rr.getPassword()));
        user.setName(rr.getName());
        user.setBirthDate(rr.getBirthDate());
        user.setPhoneNumber(rr.getPhoneNumber());
        user.setEmailVerified(false);
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setPremiumStatus(false);
        user.setPremiumExpiry(null);

        userRepo.insertUser(user);
        sendVerificationEmail(rr.getEmail(), user.getEmailVerificationToken());
    }

    public void verifyEmail(String token) {
        User user = userRepo.findByEmailVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException());
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepo.updateEmailVerificationStatus(user.getId(), user.isEmailVerified(),
                user.getEmailVerificationToken());
    }

    public String login(LoginRequest lr) {
        User user = userRepo.findByUsername(lr.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(lr.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (!user.isEmailVerified()) {
            throw new EmailUnverifiedException();
        }
        // return authentication token if login is successful
        return jwtUtil.generateToken(user.getId());
    }

    private void sendVerificationEmail(String email, String token) {

        String body = """
                Thank you for creating an account with us
                Click to verify: %s
                """.formatted(BASE_URL + token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify your email - ConcertSync");
        message.setText(body);
        mailSender.send(message);
    }
    
}
