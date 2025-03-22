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
            INSERT INTO group_members (group_id, user_id)
            VALUES (?, ?)
            """;

    private static final String SQL_REMOVE_MEMBER = """
            DELETE FROM group_members
            WHERE group_id = ? AND user_id = ?
            """;

    private static final String SQL_COUNT_MEMBERS = """
            SELECT COUNT(*) FROM group_members
            WHERE group_id = ?
            """;

    private static final String SQL_GET_GROUP_MEMBERS = """
            SELECT u.* FROM users u
            JOIN group_members gm ON u.id = gm.user_id
            WHERE gm.group_id = ?
            """;

    private static final String SQL_CHECK_MEMBERSHIP = """
            SELECT COUNT(*) FROM group_members
            WHERE group_id = ? AND user_id = ?
            """;

    private static final String SQL_GET_USER_GROUPS = """
            SELECT g.* FROM concert_groups g
            JOIN group_members gm ON g.id = gm.group_id
            WHERE gm.user_id = ?
            """;

    public boolean addMember(Long groupId, Long userId) {
        int rowsAffected = template.update(SQL_ADD_MEMBER, groupId, userId);
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

}
