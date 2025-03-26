package vttp.server.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import vttp.server.exceptions.UsernameTakenException;
import vttp.server.exceptions.InvalidCredentialsException;
import vttp.server.exceptions.InvalidTokenException;
import vttp.server.exceptions.EmailNotFoundException;
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

    private static final String BASE_URL = "http://concertsync.space/#/verify-email?token="; // TO CHANGE

    private static final String DEFAULT_PROFILE_PICTURE_URL = "/images/blank_profile_pic_320px.png";

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
        user.setProfilePictureUrl(DEFAULT_PROFILE_PICTURE_URL);
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
        String verificationLink = BASE_URL + token;
        String htmlBody = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                  <div style="text-align: center;">
                    <h2>Email Verification</h2>
                  </div>
                  <p>Hello,</p>
                  <p>Welcome to ConcertSync! Thank you for creating an account with us.</p>
                  <p>To complete your registration and start enjoying ConcertSync, please verify your email address by clicking the button below. This link will expire in 24 hours.</p>
                  <p style="text-align: center;">
                    <a href="%s"
                       style="background-color: #4a148c; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                       Verify Email
                    </a>
                  </p>
                  <p>If you did not create an account with ConcertSync, please ignore this email.</p>
                  <p>Thank you,<br>The ConcertSync Team</p>
                </body>
                </html>
                """.formatted(verificationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(email);
            helper.setSubject("Verify your email - ConcertSync");
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    public void resendVerificationEmail(String username) {
        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty())
            throw new EmailNotFoundException();

        User user = userOpt.get();

        // Check if email is already verified
        if (user.isEmailVerified())
            return;

        if (user.getEmailVerificationToken() == null) {
            user.setEmailVerificationToken(UUID.randomUUID().toString());
            userRepo.updateEmailVerificationStatus(user.getId(), false, user.getEmailVerificationToken());
        }

        sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());
    }

}
