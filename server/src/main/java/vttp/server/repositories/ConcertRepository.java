package vttp.server.repositories;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vttp.server.models.angularDto.Concert;

@Repository
public class ConcertRepository {

    @Autowired
    private JdbcTemplate template;

    private Logger logger = Logger.getLogger(ConcertRepository.class.getName());

    private static final String SQL_SELECT_ID = """
            SELECT id FROM concerts WHERE artist = ? AND venue = ? AND country = ?
            """;

    private static final String SQL_INSERT_CONCERT = """
            INSERT INTO concerts (artist, venue, country, tour, artist_id) 
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SQL_GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";

    private static final String SQL_DELETE_ORPHANED_CONCERTS = """
            DELETE c FROM concerts c
            LEFT JOIN concert_dates cd ON c.id = cd.concert_id 
            WHERE cd.concert_id IS NULL
            """;
    
    private static final String SQL_GET_ALL_CONCERTS = """
            SELECT c.id, c.artist, c.venue, c.country, c.tour, a.image_url_320, cd.date 
            FROM concerts c 
            JOIN concert_dates cd ON c.id = cd.concert_id 
            JOIN artists a ON c.artist_id = a.id
            WHERE cd.date >= ?
            """;

    public Optional<Long> getId(String artist, String venue, String country) {
        List<Long> results = template.query(SQL_SELECT_ID, 
            (rs, rowNum) -> rs.getLong("id"),
            artist, venue, country);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public void insertConcert(String artist, String venue, String country, String tour, Long artistId) {
        template.update(SQL_INSERT_CONCERT, artist, venue, country, tour, artistId);
    }

    public Long getLastInsertId() {
        return template.queryForObject(SQL_GET_LAST_INSERT_ID, Long.class);
    }

    public void deleteOrphanedConcerts() {
        int numDeleted = template.update(SQL_DELETE_ORPHANED_CONCERTS);
        logger.info("Deleted %d orphaned concerts".formatted(numDeleted));
    }

    public List<Concert> getAllUpcomingConcerts(LocalDate today) {
        Map<Long, Concert> concertMap = new HashMap<>();
        template.query(SQL_GET_ALL_CONCERTS, rs -> {
            Long id = rs.getLong("id");
            Concert concert  = concertMap.computeIfAbsent(id, k -> {
                try {
                    Concert c = new Concert();
                    c.setId(id);
                    c.setArtist(rs.getString("artist"));
                    c.setVenue(rs.getString("venue"));
                    c.setCountry(rs.getString("country"));
                    c.setTour(rs.getString("tour"));
                    c.setImageUrl320(rs.getString("image_url_320"));
                    return c;
                } catch (SQLException ex) {
                    logger.warning("Error mapping concert data for id=%d: %s".formatted(id, ex.getMessage()));
                    return null;
                }
            });
            if (concert != null) { // Guard against null from computeIfAbsent
                concert.addDates(rs.getDate("date").toLocalDate());
            }
        }, today);
        
        return new ArrayList<>(concertMap.values());
    }
    
}
