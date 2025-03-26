package vttp.server.services.bootstrap;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import vttp.server.models.Setlist;
import vttp.server.models.SetlistSet;
import vttp.server.models.SetlistSong;
import vttp.server.repositories.SetlistRepository;
import vttp.server.utilities.StringNormalization;

@Service
public class SetlistApiService {

    private static final Logger logger = Logger.getLogger(SetlistApiService.class.getName());

    private static final String SETLIST_API_URL = "https://api.setlist.fm/rest/1.0/search/setlists";

    @Value("${setlist.fm.api.key}")
    private String apiKey;

    @Autowired
    private SetlistRepository setlistRepo;

    public List<Setlist> getAllSetlists() {
        return setlistRepo.getAllSetlists();
    }

    // This is for fetching straight from repo/cache
    public List<Setlist> getSetlistsForArtist(String artistName) {
        return setlistRepo.findByArtistName(artistName);
    }

    // This is to check for renewal
    public List<Setlist> getSetlistsForArtistForSync(String artistName) {
        List<Setlist> cachedSetlists = setlistRepo.findByArtistName(artistName); // it will be sorted by eventdate

        // These should already be sorted by date
        List<Setlist> apiSetlists = fetchSetlistsFromApi(artistName);

        if (cachedSetlists.isEmpty()) {
            logger.info("No cached setlists found for artist: %s. Saving all %d from API...".formatted(
                    artistName, apiSetlists.size()));
            for (Setlist setlist : apiSetlists)
                setlistRepo.saveSetlist(setlist);
            return apiSetlists;
        }

        // Get most recent cached setlist date
        LocalDate mostRecentCachedDate = parseEventDate(cachedSetlists.get(0).getEventDate());
        logger.info("Event date for most recent cached setlist is: %s".formatted(mostRecentCachedDate));

        int savedCount = 0;

        // Save only setlists that are newer than our most recent cached one
        for (Setlist apiSetlist: apiSetlists) {
            LocalDate apiSetlistDate = parseEventDate(apiSetlist.getEventDate());
            logger.info("Event date for api setlist is: %s".formatted(apiSetlistDate));

            if (apiSetlistDate.isAfter(mostRecentCachedDate)) {
                logger.info("Saving a new setlist for artist: %s".formatted(artistName));
                setlistRepo.saveSetlist(apiSetlist);
                savedCount++;
            } else {
                logger.info("Found setlist that is already saved for artist: %s".formatted(artistName));
                break; //  Found a setlist that is already cached
            }
        }

        if (savedCount > 0) {
            logger.info("Saved %d new setlists for artist: %s".formatted(savedCount, artistName));
            // Call the fetch setlist again to get the updated setlist
            return fetchSetlistsFromApi(artistName);
        } else {
            logger.info("No new setlists found for artist: %s. Using %d cached entries.".formatted(
                artistName, cachedSetlists.size()));
            return cachedSetlists;
        }

    }

    private LocalDate parseEventDate(String eventDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(eventDate, formatter);
    }

    private List<Setlist> fetchSetlistsFromApi(String artistName) {
        List<Setlist> setlists = new ArrayList<>();
        try {
            String uri = UriComponentsBuilder.fromUriString(SETLIST_API_URL)
                    .queryParam("artistName", artistName.replaceAll(" ", "+"))
                    .queryParam("p", 1) // Currently only restrict to 1 page
                    .toUriString();

            logger.info("Query url: %s".formatted(uri));                

            RequestEntity<Void> req = RequestEntity.get(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("x-api-key", apiKey)
                    .build();

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.exchange(req, String.class);
            setlists = parseSetlistResponse(response.getBody(), artistName);

        } catch (Exception e) {
            logger.severe("Error fetching setlists from API: " + e.getMessage());
            e.printStackTrace();
        }
        return setlists;
    }

    private List<Setlist> parseSetlistResponse(String responseBody, String artistNameQuery) {
        List<Setlist> result = new ArrayList<>();

        try (JsonReader reader = Json.createReader(new StringReader(responseBody))) {
            JsonObject responseObj = reader.readObject();
            if (responseObj.containsKey("setlist")) {
                JsonArray setlistArray = responseObj.getJsonArray("setlist");
                for (JsonValue value : setlistArray) {
                    JsonObject setlistJson = value.asJsonObject();

                    if (setlistJson.containsKey("artist")) {
                        JsonObject artistJson = setlistJson.getJsonObject("artist");
                        String artistName = StringNormalization.encodingNormalization(artistJson.getString("name", ""));

                        if (!artistName.equalsIgnoreCase(artistNameQuery))
                            continue;

                        Setlist setlist = new Setlist();

                        // Extract basic info
                        setlist.setId(setlistJson.getString("id"));
                        setlist.setArtistName(artistName); // Case SENSITIVE
                        setlist.setEventDate(setlistJson.getString("eventDate"));

                        // Extract venue and country
                        if (setlistJson.containsKey("venue")) {
                            JsonObject venueJson = setlistJson.getJsonObject("venue");
                            setlist.setVenueName(StringNormalization.encodingNormalization(
                                venueJson.getString("name", "")));

                            if (venueJson.containsKey("city")) {
                                JsonObject cityJson = venueJson.getJsonObject("city");
                                setlist.setCityName(StringNormalization.encodingNormalization(
                                    cityJson.getString("name")));
                                setlist.setCountryName(StringNormalization.encodingNormalization(
                                    cityJson.getJsonObject("country").getString("name")));
                            }
                        }

                        // Extract tour name
                        if (setlistJson.containsKey("tour"))
                            setlist.setTourName(StringNormalization.encodingNormalization(
                                setlistJson.getJsonObject("tour").getString("name", "")));

                        // Extract sets and songs
                        if (setlistJson.containsKey("sets")) {
                            JsonObject setsObj = setlistJson.getJsonObject("sets");

                            if (setsObj.containsKey("set")) {
                                JsonArray setArray = setsObj.getJsonArray("set");

                                List<SetlistSet> sets = new ArrayList<>();

                                for (JsonValue setValue : setArray) {
                                    JsonObject setJson = setValue.asJsonObject();
                                    SetlistSet set = new SetlistSet();

                                    // Extract set name or encore if have
                                    if (setJson.containsKey("name")) {
                                        set.setName(setJson.getString("name", ""));
                                    } else if (setJson.containsKey("encore")) {
                                        String encoreValue = setJson.get("encore").toString();
                                        set.setName("encore " + encoreValue);
                                        set.setEncore(encoreValue);
                                    }

                                    if (setJson.containsKey("song")) {
                                        JsonArray songArray = setJson.getJsonArray("song");
                                        List<SetlistSong> songs = new ArrayList<>();

                                        for (JsonValue songValue : songArray) {
                                            JsonObject songJson = songValue.asJsonObject();
                                            SetlistSong song = new SetlistSong();

                                            song.setName(songJson.getString("name", ""));

                                            if (songJson.containsKey("info"))
                                                song.setInfo(songJson.getString("info", ""));

                                            if (songJson.containsKey("tape"))
                                                song.setTape(true);
                                            else
                                                song.setTape(false);

                                            songs.add(song);
                                        }
                                        set.setSongs(songs);
                                    }
                                    sets.add(set);
                                }
                                setlist.setSets(sets);
                            }
                        }
                        result.add(setlist);
                    }
                }
            }

        } catch (Exception e) {
            logger.severe("Error parsing setlist response for %s: %s".formatted(artistNameQuery, e.getMessage()));
            e.printStackTrace();
        }

        return result;
    }

}
