package vttp.server.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vttp.server.models.Group;

@Repository
public class GroupRepository {

    @Autowired
    private JdbcTemplate template;

    // SQL queries
    private static final String SQL_INSERT_GROUP = """
            INSERT INTO concert_groups (name, description, concert_id, concert_date, creator_id, capacity, is_public)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            
    private static final String SQL_GET_GROUP_BY_ID = """
            SELECT g.*, 
                COUNT(gm.user_id) as member_count
            FROM concert_groups g 
            LEFT JOIN group_members gm ON g.id = gm.group_id AND gm.status = 'accepted'
            WHERE g.id = ?
            GROUP BY g.id, g.name, g.description, g.concert_id, g.concert_date, 
                    g.creator_id, g.capacity, g.is_public, 
                    g.created_at, g.updated_at       
            """;
            
    private static final String SQL_UPDATE_GROUP = """
            UPDATE concert_groups 
            SET name = ?, description = ?, capacity = ?, is_public = ?
            WHERE id = ?
            """;
            
    private static final String SQL_DELETE_GROUP = """
            DELETE FROM concert_groups WHERE id = ?
            """;
            
    private static final String SQL_FIND_GROUPS_BY_CONCERT_ID = """
            SELECT g.*, 
                COUNT(gm.user_id) as member_count
            FROM concert_groups g 
            LEFT JOIN group_members gm ON g.id = gm.group_id AND gm.status = 'accepted'
            WHERE g.concert_id = ?
            GROUP BY g.id, g.name, g.description, g.concert_id, g.concert_date, 
                    g.creator_id, g.capacity, g.is_public, 
                    g.created_at, g.updated_at       
            """;
            
    private static final String SQL_FIND_GROUPS_BY_CREATOR_ID = """
            SELECT g.*, 
                COUNT(gm.user_id) as member_count
            FROM concert_groups g 
            LEFT JOIN group_members gm ON g.id = gm.group_id AND gm.status = 'accepted'
            WHERE g.creator_id = ?
            GROUP BY g.id, g.name, g.description, g.concert_id, g.concert_date, 
                    g.creator_id, g.capacity, g.is_public, 
                    g.created_at, g.updated_at       
            """;
            
    private static final String SQL_SEARCH_PUBLIC_GROUPS = """
            SELECT * FROM concert_groups 
            WHERE is_public = TRUE 
            AND name LIKE ? 
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;
            
    private static final String SQL_VALIDATE_CONCERT_DATE = """
            SELECT COUNT(*) FROM concert_dates 
            WHERE concert_id = ? AND date = ?
            """;

    private static final String SQL_GET_GROUP_MEMBER_IDS = """
        SELECT user_id FROM group_members
        WHERE group_id = ? AND status = 'accepted'
        """;

    private static final String SQL_UPDATE_GROUP_CREATOR = """
        UPDATE concert_groups SET creator_id = ?
        WHERE id = ?
        """;

    public Long insertGroup(Group group) {
        template.update(SQL_INSERT_GROUP, 
            group.getName(),
            group.getDescription(),
            group.getConcertId(),
            group.getConcertDate(),
            group.getCreatorId(),
            group.getCapacity(),
            group.isPublic());
        return template.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public Optional<Group> findById(Long id) {
        try {
            Group group = template.queryForObject(
                SQL_GET_GROUP_BY_ID,
                Group::populateFromResultSet,
                id
            );
            return Optional.of(group);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean updateGroup(Group group) {
        int rowsAffected = template.update(SQL_UPDATE_GROUP,
            group.getName(),
            group.getDescription(),
            group.getCapacity(),
            group.isPublic(),
            group.getId()
        );
        return rowsAffected > 0;
    }

    public boolean deleteGroup(Long id) {
        int rowsAffected = template.update(SQL_DELETE_GROUP, id);
        return rowsAffected > 0;
    }

    public List<Group> findByConcertId(Long concertId) {
        return template.query(SQL_FIND_GROUPS_BY_CONCERT_ID, Group::populateFromResultSet, concertId);
    }

    public List<Group> findByCreatorId(Long concertId) {
        return template.query(SQL_FIND_GROUPS_BY_CREATOR_ID, Group::populateFromResultSet, concertId);
    }

    public List<Group> searchPublicGroups(String searchTerm, int limit, int offset) {
        return template.query(SQL_SEARCH_PUBLIC_GROUPS, Group::populateFromResultSet, 
            "%" + searchTerm + "%",
            limit, offset);
    }

    public boolean isValidConcertDate(Long concertId, LocalDate date) {
        Integer count = template.queryForObject(SQL_VALIDATE_CONCERT_DATE, 
            Integer.class, 
            concertId, date);
        return count != null && count > 0;
    }

    public List<Long> getGroupMemberIds(Long groupId) {
        return template.queryForList(SQL_GET_GROUP_MEMBER_IDS, Long.class, groupId);
    }

    public boolean updateGroupCreator(Long groupId, Long newCreatorId) {
        int rowsAffected = template.update(SQL_UPDATE_GROUP_CREATOR, newCreatorId, groupId);
        return rowsAffected > 0;
    }
    
}
