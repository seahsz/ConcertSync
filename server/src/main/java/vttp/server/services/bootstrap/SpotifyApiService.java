package vttp.server.services.bootstrap;

import java.io.StringReader;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import vttp.server.models.Artist;

@Service
public class SpotifyApiService {

    private static final String TOKEN_REQUEST_URL = "https://accounts.spotify.com/api/token";

    private static final String BASE_URL = "https://api.spotify.com/v1";

    private Logger logger = Logger.getLogger(SpotifyApiService.class.getName());

    @Value("${spotify.api.client.id}")
    private String clientId;

    @Value("${spotify.api.client.secret}")
    private String clientSecret;

    private String accessToken = null;
    private long tokenExpirationTime;

    @PostConstruct
    public void init() {
        getAccessToken();
    }

    public synchronized void getAccessToken() {

        RestTemplate template = new RestTemplate();

        try {
            // Construct Request
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);

            RequestEntity<MultiValueMap<String, String>> postReq = RequestEntity.post(TOKEN_REQUEST_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body);

            // Send the request
            ResponseEntity<String> response = template.exchange(postReq, String.class);

            JsonObject json = Json.createReader(new StringReader(response.getBody())).readObject();
            accessToken = json.getString("access_token");
            int expiresIn = json.getInt("expires_in");
            tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000L) - 30000; // Refresh 5 minutes early
            logger.info("Spotify access token refreshed, expires in %d seconds".formatted(expiresIn));
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.warning("Error while fetching Spotify access token: %s".formatted(ex.getMessage()));
            accessToken = null;
        }
    }

    public Optional<Artist> getArtistSpotifyIdAndImage(String artistName) {
        // Check and refresh access token if needed
        ensureValidToken();

        String uri = UriComponentsBuilder
            .fromUriString(BASE_URL + "/search")
            .queryParam("q", artistName.toLowerCase().replaceAll(" ", "+"))
            .queryParam("type", "artist")
            .queryParam("limit", 1) // Set to 1, gave better search results
            .toUriString();

        System.out.println(uri);

        RequestEntity<Void> getReq = RequestEntity
            .get(uri)
            .header("Authorization", "Bearer %s".formatted(accessToken))
            .build();

        RestTemplate template = new RestTemplate();

        try {
            ResponseEntity<String> resp = template.exchange(getReq, String.class);
            String payload = resp.getBody();
            JsonObject artists = Json.createReader(new StringReader(payload)).readObject().getJsonObject("artists");
            JsonObject result = artists.getJsonArray("items").getJsonObject(0);
            JsonArray images = result.getJsonArray("images");
            // Populate DTO artist
            Artist newArtist = new Artist();
            newArtist.setSpotifyId(result.getString("id"));
            newArtist.setName(result.getString("name"));
            newArtist.setImageUrl640(images.getJsonObject(0).getString("url"));
            newArtist.setImageUrl320(images.getJsonObject(1).getString("url"));
            newArtist.setImageUrl160(images.getJsonObject(2).getString("url"));
            logger.info("Fetched artist: %s".formatted(newArtist.getName()));
            return Optional.of(newArtist);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.warning("Error fetching artist info for %s: %s".formatted(artistName, ex.getMessage()));
            return Optional.empty();
        }
    }

    private void ensureValidToken() {
        if (accessToken == null || System.currentTimeMillis() >= tokenExpirationTime) {
            getAccessToken();
        }
    }

}
