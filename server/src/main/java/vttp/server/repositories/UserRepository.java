package vttp.server.repositories;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vttp.server.exceptions.UserNotFoundException;
import vttp.server.models.User;

@Repository
public class UserRepository {

    private static final String SQL_INSERT_USER = """
            INSERT INTO users (username, email, password, name, birth_date, profile_picture_url,
            phone_number, email_verified, email_verification_token, premium_status, premium_expiry)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_FIND_BY_ID = """
            SELECT * FROM users WHERE id = ?
            """;

    private static final String SQL_FIND_BY_EMAIL = """
            SELECT * FROM users WHERE email = ?
            """;

    private static final String SQL_FIND_BY_USERNAME = """
            SELECT * FROM users WHERE username = ?
            """;

    private static final String SQL_FIND_BY_TOKEN = """
            SELECT * FROM users WHERE email_verification_token = ?
            """;

    private static final String SQL_UPDATE_EMAIL_VERIFICATION = """
            UPDATE users SET email_verified = ?, email_verification_token = ?,
            updated_at = CURRENT_TIMESTAMP WHERE id = ?
            """;
    
    private static final String SQL_UPDATE_PHONE_NUMBER = """
            UPDATE users SET phone_number = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?
            """;

    private static final String SQL_UPDATE_NAME = """
            UPDATE users SET name = ?, last_name_update = ?,
            updated_at = CURRENT_TIMESTAMP WHERE id = ?
            """;

    private static final String SQL_UPDATE_PROFILE_PICTURE_URL = """
            UPDATE users SET profile_picture_url = ?, 
            updated_at = CURRENT_TIMESTAMP WHERE id = ?
            """;

    private Logger logger = Logger.getLogger(UserRepository.class.getName());

    @Autowired
    private JdbcTemplate template;

    public void insertUser(User user) {
        template.update(SQL_INSERT_USER, user.getUsername(), user.getEmail(),
            user.getPassword(), user.getName(), user.getBirthDate(), 
            user.getProfilePictureUrl(), user.getPhoneNumber(), 
            user.isEmailVerified(), user.getEmailVerificationToken(), 
            user.isPremiumStatus(), user.getPremiumExpiry());
    }

    public Optional<User> findById(Long id) {
        try {
            User user = template.queryForObject(SQL_FIND_BY_ID, User::populateFromResultSet, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found for id %d: %s".formatted(id, ex.getMessage()));
            return Optional.empty();
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = template.queryForObject(SQL_FIND_BY_EMAIL, User::populateFromResultSet, email);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found for email %s: %s".formatted(email, ex.getMessage()));
            return Optional.empty();
        }
    }

    public Optional<User> findByUsername(String username) {
        try {
            User user = template.queryForObject(SQL_FIND_BY_USERNAME, User::populateFromResultSet, username);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found for token %s: %s".formatted(username, ex.getMessage()));
            return Optional.empty();
        }
    }

    public Optional<User> findByEmailVerificationToken(String token) {
        try {
            User user = template.queryForObject(SQL_FIND_BY_TOKEN, User::populateFromResultSet, token);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found for token %s: %s".formatted(token, ex.getMessage()));
            return Optional.empty();
        }
    }

    // Individual updates
    public void updateEmailVerificationStatus(long id, boolean verified, String token) {
        int rowUpdated = template.update(SQL_UPDATE_EMAIL_VERIFICATION, verified, token, id);
        if (rowUpdated == 0)
            throw new UserNotFoundException(id);
    }

    public void updatePhoneNumber(long id, String phoneNumber) {
        int rowUpdated = template.update(SQL_UPDATE_PHONE_NUMBER, phoneNumber, id);
        if (rowUpdated == 0)
            throw new UserNotFoundException(id);
    }

    public void updateName(long id, String name, LocalDate lastNameUpdate) {
        int rowUpdated = template.update(SQL_UPDATE_NAME, name, lastNameUpdate, id);
        if (rowUpdated == 0)
            throw new UserNotFoundException(id);
    }

    public void updateProfilePicture(long id, String profilePicUrl) {
        int rowUpdated = template.update(SQL_UPDATE_PROFILE_PICTURE_URL, profilePicUrl, id);
        if (rowUpdated == 0)
            throw new UserNotFoundException(id);
    }

    
}
