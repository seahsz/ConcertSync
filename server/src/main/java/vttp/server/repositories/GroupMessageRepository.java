package vttp.server.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vttp.server.models.GroupMessage;

@Repository
public class GroupMessageRepository {

    @Autowired
    private JdbcTemplate template;

    private static final String SQL_ADD_MESSAGE = """
            INSERT INTO group_messages (group_id, user_id, message)
            VALUES (?, ?, ?)
            """;

    private static final String SQL_GET_MESSAGES = """
            SELECT * FROM group_messages
            WHERE group_id = ?
            ORDER BY created_at ASC
            LIMIT ? OFFSET ?
            """;

    private static final String SQL_DELETE_MESSAGE = """
            DELETE FROM group_messages
            WHERE id = ? AND user_id = ?
            """;

    public Long addMessage(GroupMessage message) {
        template.update(
                SQL_ADD_MESSAGE,
                message.getGroupId(),
                message.getUserId(),
                message.getMessage());
        return template.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public List<GroupMessage> getMessages(Long groupId, int limit, int offset) {
        return template.query(
                SQL_GET_MESSAGES,
                GroupMessage::mapRowToGroupMessage,
                groupId,
                limit,
                offset);
    }

    public boolean deleteMessage(Long messageId, Long userId) {
        int rowsAffected = template.update(SQL_DELETE_MESSAGE, messageId, userId);
        return rowsAffected > 0;
    }

}
