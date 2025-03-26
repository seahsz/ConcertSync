package vttp.server.repositories;

import java.util.List;
import java.util.Optional;

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

    private static final String SQL_GET_MESSAGES_WITH_USER_DETAILS = """
            SELECT gm.*, u.username, u.name, u.profile_picture_url
            FROM group_messages gm
            JOIN users u ON gm.user_id = u.id
            WHERE gm.group_id = ?
            ORDER BY gm.created_at ASC
            LIMIT ? OFFSET ?
            """;

    private static final String SQL_GET_MESSAGE_BY_ID = """
            SELECT gm.*, u.username, u.name, u.profile_picture_url
            FROM group_messages gm
            JOIN users u ON gm.user_id = u.id
            WHERE gm.id = ?
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

    public List<GroupMessage> getMessagesWithUserDetails(Long groupId, int limit, int offset) {
        return template.query(
                SQL_GET_MESSAGES_WITH_USER_DETAILS,
                GroupMessage::mapRowToGroupMessageWithUserDetails,
                groupId,
                limit,
                offset);
    }

    public Optional<GroupMessage> getMessageById(Long messageId) {
        try {
            GroupMessage message = template.queryForObject(
                SQL_GET_MESSAGE_BY_ID,
                GroupMessage::mapRowToGroupMessageWithUserDetails,
                messageId
            );
            return Optional.ofNullable(message);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
