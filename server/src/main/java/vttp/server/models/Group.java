package vttp.server.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Group {

    private Long id;
    private String name;
    private String description;
    private Long concertId;
    private LocalDate concertDate;
    private Long creatorId;
    private int capacity;
    private int memberCount;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }
    
    public LocalDate getConcertDate() { return concertDate; }
    public void setConcertDate(LocalDate concertDate) { this.concertDate = concertDate; }
    
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Static method to populate from ResultSet
    public static Group populateFromResultSet(ResultSet rs, int rowNum) throws SQLException {
        Group group = new Group();
        group.setId(rs.getLong("id"));
        group.setName(rs.getString("name"));
        group.setDescription(rs.getString("description"));
        group.setConcertId(rs.getLong("concert_id"));
        group.setConcertDate(rs.getDate("concert_date").toLocalDate());
        group.setCreatorId(rs.getLong("creator_id"));
        group.setMemberCount(rs.getInt("member_count"));
        group.setCapacity(rs.getInt("capacity"));
        group.setPublic(rs.getBoolean("is_public"));
        group.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        group.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return group;
    }

    // Static method to map Group to JsonObject
    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("id", id)
            .add("name", name)
            .add("description", description)
            .add("concertId", concertId)
            .add("concertDate", concertDate.toString())
            .add("creatorId", creatorId)
            .add("memberCount", memberCount)
            .add("capacity", capacity)
            .add("isPublic", isPublic)
            .add("createdat", createdAt.toString());

        return builder.build();
    }
    
}
