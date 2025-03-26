package vttp.server.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vttp.server.models.Artist;

@Repository
public class ArtistRepository {

    private static final String SQL_UPSERT_ARTIST = """
            INSERT INTO artists (name, spotify_id, image_url_640, image_url_320, image_url_160)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            spotify_id = VALUES(spotify_id), image_url_640 = VALUES(image_url_640),
            image_url_320 = VALUES(image_url_320), image_url_160 = VALUES(image_url_160)
            """;

    private static final String SQL_FIND_BY_ID = """
        SELECT * FROM artists WHERE id = ?
        """;

    private static final String SQL_FIND_BY_NAME = """
        SELECT * FROM artists WHERE name = ?
        """;

    private static final String SQL_GET_ALL_ARTIST_NAMES = """
            SELECT name FROM artists WHERE name != 'not_found'
            """;

    @Autowired
    private JdbcTemplate template;

    public void upsertArtist(Artist artist) {
        template.update(SQL_UPSERT_ARTIST, artist.getName(), artist.getSpotifyId(),
                artist.getImageUrl640(), artist.getImageUrl320(), artist.getImageUrl160());
    }

    public Optional<Artist> findById(Long id) {
        try {
            Artist artist = template.queryForObject(
                SQL_FIND_BY_ID, 
                Artist::populateFromResultSet, 
                id
            );
            return Optional.of(artist);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<Artist> findByName(String name) {
        try {
            Artist artist = template.queryForObject(
                SQL_FIND_BY_NAME, 
                Artist::populateFromResultSet, 
                name
            );
            return Optional.of(artist);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<String> getAllArtistNames() {
        return template.queryForList(SQL_GET_ALL_ARTIST_NAMES, String.class);
    }

}
