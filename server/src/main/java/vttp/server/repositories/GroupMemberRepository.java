package vttp.server.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vttp.server.models.Group;
import vttp.server.models.User;

@Repository
public class GroupMemberRepository {

    @Autowired
    private JdbcTemplate template;

    private static final String SQL_ADD_MEMBER = """
            INSERT INTO group_members (group_id, user_id, status)
            VALUES (?, ?, ?)
            """;

    private static final String SQL_REMOVE_MEMBER = """
            DELETE FROM group_members
            WHERE group_id = ? AND user_id = ?
            """;

    private static final String SQL_COUNT_MEMBERS = """
            SELECT COUNT(*) FROM group_members
            WHERE group_id = ? AND status = 'accepted'
            """;

    private static final String SQL_GET_GROUP_MEMBERS = """
            SELECT u.* FROM users u
            JOIN group_members gm ON u.id = gm.user_id
            WHERE gm.group_id = ? AND gm.status = 'accepted'
            """;

    private static final String SQL_CHECK_MEMBERSHIP = """
            SELECT COUNT(*) FROM group_members
            WHERE group_id = ? AND user_id = ?
            """;

    private static final String SQL_GET_USER_GROUPS = """
        SELECT g.*, 
            COUNT(gm2.user_id) as member_count
        FROM concert_groups g
        JOIN group_members gm ON g.id = gm.group_id
        LEFT JOIN group_members gm2 ON g.id = gm2.group_id AND gm2.status = 'accepted'
        WHERE gm.user_id = ?
        GROUP BY g.id, g.name, g.description, g.concert_id, g.concert_date, 
                g.creator_id, g.capacity, g.is_public, 
                g.created_at, g.updated_at
        """;

    private static final String SQL_GET_PENDING_REQUESTS = """
            SELECT u.* FROM users u
            JOIN group_members gm ON u.id = gm.user_id
            WHERE gm.group_id = ? AND gm.status = 'pending'
            """;

    private static final String SQL_UPDATE_REQUEST_STATUS = """
            UPDATE group_members
            SET status = ?
            WHERE group_id = ? AND user_id = ?
            """;

    private static final String SQL_GET_MEMBERSHIP_STATUS = """
            SELECT status FROM group_members
            WHERE group_id = ? AND user_id = ?
            """;

    public boolean addMember(Long groupId, Long userId, String status) {
        int rowsAffected = template.update(SQL_ADD_MEMBER, groupId, userId, status);
        return rowsAffected > 0;
    }

    public boolean removeMember(Long groupId, Long userId) {
        int rowsAffected = template.update(SQL_REMOVE_MEMBER, groupId, userId);
        return rowsAffected > 0;
    }

    public int countMembers(Long groupId) {
        Integer count = template.queryForObject(SQL_COUNT_MEMBERS, Integer.class, groupId);
        return count != null ? count : 0; // Handle potential null value
    }

    public List<User> getGroupMembers(Long groupId) {
        return template.query(SQL_GET_GROUP_MEMBERS,
                User::populateFromResultSet,
                groupId);
    }

    public boolean isMember(Long groupId, Long userId) {
        Integer count = template.queryForObject(SQL_CHECK_MEMBERSHIP, Integer.class,
                groupId, userId);
        return count != null && count > 0;
    }

    public List<Group> getUserGroup(Long userId) {
        return template.query(SQL_GET_USER_GROUPS, Group::populateFromResultSet, userId);
    }

    public List<User> getPendingRequests(Long groupId) {
        return template.query(SQL_GET_PENDING_REQUESTS,
                User::populateFromResultSet,
                groupId);
    }

    // Method to approve/reject a join request
    public boolean updateRequestStatus(Long groupId, Long userId, String status) {
        int rowsAffected = template.update(SQL_UPDATE_REQUEST_STATUS,
                status, groupId, userId);
        return rowsAffected > 0;
    }

    // New method to get membership status
    public String getMembershipStatus(Long groupId, Long userId) {
        try {
            return template.queryForObject(
                SQL_GET_MEMBERSHIP_STATUS,
                String.class,
                groupId, userId
            );
        } catch (Exception e) {
            // If no record found, user is not a member
            return "not-member";
        }
    }
}
