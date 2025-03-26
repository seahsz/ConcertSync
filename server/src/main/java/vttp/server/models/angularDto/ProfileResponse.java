package vttp.server.models.angularDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import vttp.server.models.User;

// Created ProfileResponse to return only necessary fields
public class ProfileResponse {

    private String email;
    private String username;
    private String name;
    private String phoneNumber;
    private String profilePictureUrl;
    private LocalDate birthDate;
    private boolean premiumStatus;
    private LocalDate premiumExpiry;
    private String subscriptionId;
    private boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDate lastNameUpdate;

    public ProfileResponse(User user) {
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.name = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.profilePictureUrl = user.getProfilePictureUrl();
        this.birthDate = user.getBirthDate();
        this.premiumStatus = user.isPremiumStatus();
        this.premiumExpiry = user.getPremiumExpiry();
        this.subscriptionId = user.getSubscriptionId();
        this.autoRenew = user.isAutoRenew();
        this.createdAt = user.getCreatedAt();
        this.lastNameUpdate = user.getLastNameUpdate();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isPremiumStatus() { return premiumStatus; }
    public void setPremiumStatus(boolean premiumStatus) { this.premiumStatus = premiumStatus; }

    public LocalDate getPremiumExpiry() { return premiumExpiry; }
    public void setPremiumExpiry(LocalDate premiumExpiry) { this.premiumExpiry = premiumExpiry; }

    public LocalDate getLastNameUpdate() { return lastNameUpdate; }
    public void setLastNameUpdate(LocalDate lastNameUpdate) { this.lastNameUpdate = lastNameUpdate; }
    
    public String getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(String subscriptionId) { this.subscriptionId = subscriptionId; }

    public boolean isAutoRenew() { return autoRenew; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }

    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("username", username)
            .add("email", email)
            .add("name", name)
            .add("birth_date", birthDate.toString())
            .add("profile_picture_url", profilePictureUrl == null ? "/images/blank_profile_pic_320px" : profilePictureUrl)
            .add("phone_number", phoneNumber == null ? "" : phoneNumber)
            .add("created_at", createdAt.toString())
            .add("premium_status", premiumStatus)
            .add("premium_expiry", premiumExpiry == null ? "" : premiumExpiry.toString())
            .add("last_name_update", lastNameUpdate.toString());

        if (premiumStatus) {
            builder.add("subscription_id", subscriptionId == null ? "" : subscriptionId)
                .add("auto_renew", autoRenew);
        }

        return builder.build();
    }
}
