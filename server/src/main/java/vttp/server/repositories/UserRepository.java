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

    // To track and update number of groups created - for premium feature tracking (premium create 10 grps, non-premium 2)
    private static final String SQL_INCREMENT_GROUPS_CREATED = """
            UPDATE users
            SET groups_created = groups_created + 1
            WHERE id = ?
            """;

    private static final String SQL_GET_GROUPS_CREATED = """
            SELECT groups_created FROM users
            WHERE id = ?
            """;

    // For premium subscription handling
    private static final String SQL_UPDATE_SUBSCRIPTION = """
        UPDATE users 
        SET premium_status = ?, premium_expiry = ?, subscription_id = ?, auto_renew = ?,
        updated_at = CURRENT_TIMESTAMP 
        WHERE id = ?
        """;

    private static final String SQL_FIND_BY_SUBSCRIPTION_ID = """
        SELECT * FROM users WHERE subscription_id = ?
        """;

    // User deletion
    private static final String SQL_DELETE_USER = """
        DELETE FROM users WHERE id = ?
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

    // Tracking num of groups created
    public boolean incrementGroupsCreated(Long userId) {
        int rowsUpdated = template.update(SQL_INCREMENT_GROUPS_CREATED, userId);
        return rowsUpdated > 0;
    }

    public int getGroupsCreated(Long userId) {
        try {
            Integer count = template.queryForObject(SQL_GET_GROUPS_CREATED, Integer.class, userId);
            return count != null ? count : 0;
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found for id %d when getting groups created: %s".formatted(userId, ex.getMessage()));
            throw new UserNotFoundException(userId);
        }
    }

    public void updateSubscription(Long userId, boolean premiumStatus, LocalDate expiryDate, 
                              String subscriptionId, boolean autoRenew) {
        int rowsUpdated = template.update(SQL_UPDATE_SUBSCRIPTION, 
            premiumStatus, expiryDate, subscriptionId, autoRenew, userId);
        if (rowsUpdated == 0)
            throw new UserNotFoundException(userId);
    }

    public Optional<User> findBySubscriptionId(String subscriptionId) {
        try {
            User user = template.queryForObject(SQL_FIND_BY_SUBSCRIPTION_ID, 
                                              User::populateFromResultSet, 
                                              subscriptionId);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No user found for subscription ID " + subscriptionId);
            return Optional.empty();
        }
    }

    public boolean deleteUser(Long userId) {
        try {
            int rowsAffected = template.update(SQL_DELETE_USER, userId);
            logger.info("Deleted user ID: " + userId + ", affected rows: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.severe("Error deleting user with ID: " + userId + ", error: " + e.getMessage());
            return false;
        }
    }

}
