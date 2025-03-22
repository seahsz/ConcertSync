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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vttp.server.models.angularDto.Concert;
import vttp.server.utilities.StringSimilarity;

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

    private static final String SQL_SELECT_ID_AND_VENUE = """
            SELECT id, venue FROM concerts
            WHERE artist = ? AND country = ?
            """;

    private static final String SQL_FIND_BY_ID = """
            SELECT id FROM concerts WHERE id = ?
            """;

    private static final String SQL_GET_CONCERT_BY_ID = """
            SELECT c.id, c.artist, c.venue, c.country, c.tour, a.image_url_320
            FROM concerts c
            JOIN artists a ON c.artist_id = a.id
            WHERE c.id = ?
            """;

    private static final String SQL_GET_CONCERT_DATES = """
            SELECT date
            FROM concert_dates
            WHERE concert_id = ?
            ORDER BY date ASC
            """;

    private double SIMILARITY_THRESHOLD = 0.8;

    public Optional<Long> getId(String artist, String venue, String country) {
        List<Long> results = template.query(SQL_SELECT_ID,
                (rs, rowNum) -> rs.getLong("id"),
                artist, venue, country);

        if (!results.isEmpty())
            return Optional.of(results.get(0));

        // If no exact match, try to find venues with similar names
        List<Map<String, Object>> venues = template.queryForList(SQL_SELECT_ID_AND_VENUE, artist, country);

        // Check for similar venues -> in case of weird changes to venue name in
        // setlist.fm
        // Prevents insertion of new concert for essentially the same concert
        // Example: Phil's Studio vs Phil Studio
        for (Map<String, Object> row : venues) {
            String existingVenue = (String) row.get("venue");

            double similarity = StringSimilarity.calculateSimilarity(
                    venue.toLowerCase(), existingVenue.toLowerCase());

            if (similarity > SIMILARITY_THRESHOLD) {
                logger.info("Found similar venue: %s matches %s with similarity %s".formatted(
                        venue, existingVenue, similarity));
                return Optional.of((Long) row.get("id"));
            }
        }

        return Optional.empty();
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
            Concert concert = concertMap.computeIfAbsent(id, k -> {
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

    public Optional<Long> findById(Long id) {
        try {
            Long concertId = template.queryForObject(SQL_FIND_BY_ID, Long.class, id);
            return Optional.ofNullable(concertId);
        } catch (EmptyResultDataAccessException ex) {
            logger.warning("No concert found for id %d: %s".formatted(id, ex.getMessage()));
            return Optional.empty();
        }
    }

    public Optional<Concert> getConcertById(Long id) {
        try {
            // First, check if the concert exists
            if (!findById(id).isPresent()) {
                return Optional.empty();
            }

            // Query for the concert details
            Concert concert = template.queryForObject(SQL_GET_CONCERT_BY_ID,
                    (rs, rowNum) -> {
                        Concert c = new Concert();
                        c.setId(rs.getLong("id"));
                        c.setArtist(rs.getString("artist"));
                        c.setVenue(rs.getString("venue"));
                        c.setCountry(rs.getString("country"));
                        c.setTour(rs.getString("tour"));
                        c.setImageUrl320(rs.getString("image_url_320"));
                        return c;
                    },
                    id);

            // Query for the dates associated with this concert
            List<LocalDate> dates = template.query(SQL_GET_CONCERT_DATES,
                    (rs, rowNum) -> rs.getDate("date").toLocalDate(),
                    id);

            // Add the dates to the concert object
            concert.setDates(dates);

            return Optional.of(concert);
        } catch (EmptyResultDataAccessException e) {
            logger.warning("No concert found with ID: " + id);
            return Optional.empty();
        } catch (Exception e) {
            logger.warning("Error fetching concert with ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean validateConcertDate(Long concertId, LocalDate date) {
        try {
            Integer count = template.queryForObject(
                    "SELECT COUNT(*) FROM concert_dates WHERE concert_id = ? AND date = ?",
                    Integer.class,
                    concertId, date);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.warning("Error validating concert date: " + e.getMessage());
            return false;
        }
    }
}
