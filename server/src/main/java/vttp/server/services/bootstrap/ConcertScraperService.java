package vttp.server.services.bootstrap;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import vttp.server.models.Artist;
import vttp.server.repositories.ArtistRepository;
import vttp.server.repositories.ConcertDateRepository;
import vttp.server.repositories.ConcertRepository;
import vttp.server.utilities.StringNormalization;

@Service
public class ConcertScraperService {

    private static final Logger logger = Logger.getLogger(ConcertScraperService.class.getName());
    private static final String BASE_URL = "https://www.setlist.fm/search?country=sg&upcoming=true&page=%d";
    private static final String COUNTRY = "Singapore";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy");

    private static final String NOT_FOUND_SPOTIFY_ID = "not_found";
    private static final String NOT_FOUND_NAME = "not_found";
    private static final String PLACEHOLDER_640 = "/images/blank_profile_pic_640px.png";
    private static final String PLACEHOLDER_320 = "/images/blank_profile_pic_320px.png";
    private static final String PLACEHOLDER_160 = "/images/blank_profile_pic_160px.png";

    // Thread-safe cache for artist lookups
    private Map<String, Long> artistCache = new ConcurrentHashMap<>();

    private Long notFoundArtistId;

    @Autowired
    private SpotifyApiService spotifyApiSvc;

    @Autowired
    private ConcertRepository concertRepo;

    @Autowired
    private ConcertDateRepository concertDateRepo;

    @Autowired
    private ArtistRepository artistRepo;

    // Insert a default not_found artist in case artist name does not exist in spotify
    @PostConstruct
    public void init() {
        initializeNotFoundArtist();

        new Thread(this::scrapUpcomingConcerts).start();
    }

    private synchronized void initializeNotFoundArtist() {
        try {
            Optional<Artist> existingArtist = artistRepo.findByName(NOT_FOUND_NAME);
            
            if (existingArtist.isEmpty()) {
                Artist notFoundArtist = new Artist();
                notFoundArtist.setSpotifyId(NOT_FOUND_SPOTIFY_ID);
                notFoundArtist.setName(NOT_FOUND_NAME);
                notFoundArtist.setImageUrl640(PLACEHOLDER_640);
                notFoundArtist.setImageUrl320(PLACEHOLDER_320);
                notFoundArtist.setImageUrl160(PLACEHOLDER_160);

                artistRepo.upsertArtist(notFoundArtist);
                
                // Retrieve the artist we just inserted
                existingArtist = artistRepo.findByName(NOT_FOUND_NAME);
            } 

            notFoundArtistId = existingArtist
                .map(Artist::getId)
                .orElseThrow(() -> new RuntimeException("Could not find or create not_found artist"));

            // Cache the ID
            artistCache.put(NOT_FOUND_NAME, notFoundArtistId);
        } catch (Exception e) {
            logger.severe("Error initializing not found artist: %s".formatted(e.getMessage()));
            throw new RuntimeException("Failed to initialize not_found artist", e);
        }
    }

    private Long getOrCreateArtistId(String artistName) {
        return artistCache.computeIfAbsent(artistName, name -> {
            name = StringNormalization.encodingNormalization(artistName);

            try {
                // Try to find artist in spotify
                Optional<Artist> existingArtist = spotifyApiSvc.getArtistSpotifyIdAndImage(artistName);

                if (existingArtist.isPresent()) {
                    Artist spotifyArtist = existingArtist.get();

                    // Ensure the name matches closesly
                    if (name.trim().equalsIgnoreCase(spotifyArtist.getName().trim())) {
                        artistRepo.upsertArtist(spotifyArtist);

                        // Find the artist by name to get the MySQL "id" from artists table
                        Optional<Artist> insertedArtist = artistRepo.findByName(spotifyArtist.getName());
                        return insertedArtist
                            .map(Artist::getId)
                            .orElse(notFoundArtistId);
                    }
                }

                return notFoundArtistId; // as a fallback if no matching artist was found in spotify

            } catch (Exception e) {
                logger.warning("Error getting artist ID for %s: %s".formatted(name, e.getMessage()));
                return notFoundArtistId;
            }
        });
    }

    private Long getConcertId(String artist, String venue, String country, String tour, Long artistId) {
        // Try to find existing concert
        Optional<Long> existingConcertId = concertRepo.getId(artist, venue, country);

        if (existingConcertId.isPresent())
            return existingConcertId.get();
        
        // Insert new concert if not found
        concertRepo.insertConcert(artist, venue, country, tour, artistId);

        // Find the concert by unique constraints
        return concertRepo.getId(artist, venue, country)
            .orElseThrow(() -> new RuntimeException("Failed to insert concert"));
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    public void scrapUpcomingConcerts() {
        logger.info("Starting web scraping for upcoming concerts...");
        logger.info(" ");

        List<Object[]> concertDatesArgs = new ArrayList<>();
        int page = 1;
        LocalDate today = LocalDate.now();
        boolean hasPastDate = false;

        try {
            cleanupPastConcerts(today);

            while (!hasPastDate) {
                String currentUrl = String.format(BASE_URL, page);
                logger.info("Scraping page: %s".formatted(currentUrl));

                Document doc = Jsoup.connect(currentUrl).get();
                Elements events = doc.select(".col-xs-12.setlistPreview");

                if (events.isEmpty()) {
                    logger.info("No more events found on page %d. Stopping".formatted(page));
                    break;
                }

                for (Element e : events) {
                    String artist = e.select("span:contains(Artist: ) > strong > a > span").text();
                    String tour = e.select("span:contains(Tour: ) > strong > a").text();
                    // Only take the initial part (e.g. Singapore Indoor Stadium)
                    String venue = e.select("span:contains(Venue: ) > strong > a > span").text().split(",")[0];
                    String country = COUNTRY;
                    String month = e.select(".month").text();
                    String day = e.select(".day").text();
                    String year = e.select(".year").text();
                    LocalDate concertDate = LocalDate.parse(String.format("%s %s %s", day, month, year),
                            DATE_FORMATTER);
                    
                    // Normalize the strings
                    artist = StringNormalization.encodingNormalization(artist);
                    tour = StringNormalization.encodingNormalization(tour);
                    venue = StringNormalization.encodingNormalization(venue);
                    country = StringNormalization.encodingNormalization(country);

                    if (concertDate.isBefore(today)) {
                        hasPastDate = true;
                        break;
                    }

                    // Get or create artist Id
                    Long artistId = getOrCreateArtistId(artist);

                    // Get or create concert id
                    Long concertId = getConcertId(artist, venue, country, tour, artistId);

                    concertDatesArgs.add(new Object[]{ concertId, concertDate });
                }

                page++;
                Thread.sleep(Duration.ofSeconds(2).toMillis());
            }

            // Batch insert dates
            if (!concertDatesArgs.isEmpty())
                concertDateRepo.batchInsertDates(concertDatesArgs);

        } catch (IOException e) {
            logger.warning("Error during scraping: ".formatted(e.getMessage()));
        } catch (InterruptedException e) {
            logger.warning("Error while putting thread to sleep: ".formatted(e.getMessage()));
        }
    }

    private void cleanupPastConcerts(LocalDate today) {
        logger.info("Cleaning up past concert dates before %s".formatted(today));
        // Delete past dates
        concertDateRepo.deletePastDates(today);
        // Delete orphaned concerts
        concertRepo.deleteOrphanedConcerts();
    }

}
