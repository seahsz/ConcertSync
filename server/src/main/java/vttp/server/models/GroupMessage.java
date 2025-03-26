package vttp.server.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class GroupMessage {
    private Long id;
    private Long groupId;
    private Long userId;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User details for joined queries
    private String username;
    private String name;
    private String profilePictureUrl;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // User details getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public static GroupMessage mapRowToGroupMessage(ResultSet rs, int rowNum) throws SQLException {
        GroupMessage message = new GroupMessage();
        message.setId(rs.getLong("id"));
        message.setGroupId(rs.getLong("group_id"));
        message.setUserId(rs.getLong("user_id"));
        message.setMessage(rs.getString("message"));
        message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        message.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return message;
    }

    public static GroupMessage mapRowToGroupMessageWithUserDetails(ResultSet rs, int rowNum) throws SQLException {
        GroupMessage message = mapRowToGroupMessage(rs, rowNum);
        message.setUsername(rs.getString("username"));
        message.setName(rs.getString("name"));
        message.setProfilePictureUrl(rs.getString("profile_picture_url"));
        return message;
    }

    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("id", id)
            .add("groupId", groupId)
            .add("userId", userId)
            .add("message", message)
            .add("createdAt", createdAt.toString());

        // Add user details if available
        if (username != null)
            builder.add("username", username);
        if (name != null)
            builder.add("name", name);
        if (profilePictureUrl != null)
            builder.add("profilePictureUrl", profilePictureUrl);
        else
            builder.add("profilePictureUrl", "images/blank_profile_pic_160px.png");

        return builder.build();
    }


}