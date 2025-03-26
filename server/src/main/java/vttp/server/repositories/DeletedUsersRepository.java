package vttp.server.repositories;

import java.time.LocalDate;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DeletedUsersRepository {

    private Logger logger = Logger.getLogger(DeletedUsersRepository.class.getName());

    @Autowired
    private JdbcTemplate template;

    private static final String SQL_INSERT_DELETED_USER = """
            INSERT INTO deleted_users
            (original_user_id, username, email, name, birth_date, phone_number, groups_json)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    public boolean recordDeletedUser(Long userId, String username, String email,
            String name, LocalDate birthDate, String phoneNumber,
            String groupsJson) {
        try {
            int rowsAffected = template.update(SQL_INSERT_DELETED_USER,
                    userId,
                    username,
                    email,
                    name,
                    birthDate,
                    phoneNumber,
                    groupsJson);

            logger.info("Recorded deleted user with ID: " + userId);
            return rowsAffected > 0;
        } catch (Exception e) {
            logger.severe("Error recording deleted user: " + e.getMessage());
            return false;
        }
    }

}
