package vttp.server.models.angularDto;

import java.time.LocalDate;

public class CreateGroupRequest {
    private String name;
    private String description;
    private Long concertId;
    private LocalDate concertDate;
    private Integer capacity;
    private boolean isPublic = true; // ONLY HAVE PUBLIC GROUPS NOW. Doesn't make sense for private groups to exist
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getConcertId() { return concertId; }
    public void setConcertId(Long concertId) { this.concertId = concertId; }
    
    public LocalDate getConcertDate() { return concertDate; }
    public void setConcertDate(LocalDate concertDate) { this.concertDate = concertDate; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
}
