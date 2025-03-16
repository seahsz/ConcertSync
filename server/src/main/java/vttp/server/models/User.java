package vttp.server.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String name;
    private LocalDate birthDate;
    private String profilePictureUrl;
    private String phoneNumber;
    private boolean emailVerified;
    private String emailVerificationToken;
    private boolean premiumStatus;
    private LocalDate premiumExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate lastNameUpdate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) { this.emailVerificationToken = emailVerificationToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPremiumStatus() { return premiumStatus; }
    public void setPremiumStatus(boolean premiumStatus) { this.premiumStatus = premiumStatus; }

    public LocalDate getPremiumExpiry() { return premiumExpiry; }
    public void setPremiumExpiry(LocalDate premiumExpiry) { this.premiumExpiry = premiumExpiry; }

    public LocalDate getLastNameUpdate() { return lastNameUpdate; }
    public void setLastNameUpdate(LocalDate lastNameUpdate) { this.lastNameUpdate = lastNameUpdate; }
   
    public static User populateFromResultSet(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setBirthDate(rs.getDate("birth_date").toLocalDate());
        user.setProfilePictureUrl(rs.getString("profile_picture_url"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setEmailVerificationToken(rs.getString("email_verification_token"));
        user.setPremiumStatus(rs.getBoolean("premium_status"));
        user.setPremiumExpiry(rs.getDate("premium_expiry") != null ? rs.getDate("premium_expiry").toLocalDate() : null);
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        user.setLastNameUpdate(rs.getDate("last_name_update").toLocalDate());
        return user;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", email=" + email + ", password=" + password + ", name="
                + name + ", birthDate=" + birthDate + ", profilePictureUrl=" + profilePictureUrl + ", phoneNumber="
                + phoneNumber + ", emailVerified=" + emailVerified + ", emailVerificationToken="
                + emailVerificationToken + ", premiumStatus=" + premiumStatus + ", premiumExpiry=" + premiumExpiry
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", lastNameUpdate=" + lastNameUpdate + "]";
    }

    

}
