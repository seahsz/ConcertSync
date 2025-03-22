package vttp.server.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class GroupMessage {
    private Long id;
    private Long groupId;
    private Long userId;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
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
}