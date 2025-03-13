package vttp.server.repositories;

import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String SQL_GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";


    @Autowired
    private JdbcTemplate template;

    public void upsertArtist(Artist artist) {
        template.update(SQL_UPSERT_ARTIST, artist.getName(), artist.getSpotifyId(),
            artist.getImageUrl640(), artist.getImageUrl320(), artist.getImageUrl160());
    }

    public Long getLastInsertId() {
        return template.queryForObject(SQL_GET_LAST_INSERT_ID, Long.class);
    }
    
}
