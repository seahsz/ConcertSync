package vttp.server.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConcertDateRepository {

    @Autowired
    private JdbcTemplate template;

    Logger logger = Logger.getLogger(ConcertDateRepository.class.getName());
    
    private static final String SQL_INSERT_DATES = """
            INSERT IGNORE INTO concert_dates (concert_id, date) VALUES (?, ?)
            """;

    private static final String SQL_DELETE_DATES = """
            DELETE FROM concert_dates WHERE date < ?
            """;
    
    public void batchInsertDates(List<Object[]> dateArgs) {
        template.batchUpdate(SQL_INSERT_DATES, dateArgs);
        logger.info("Batch inserted %d concert dates".formatted(dateArgs.size()));
    }

    public void deletePastDates(LocalDate today) {
        int numDeleted = template.update(SQL_DELETE_DATES, today);
        logger.info("Deleted %d dates older than %s".formatted(numDeleted, today.toString()));
    }
    
}
