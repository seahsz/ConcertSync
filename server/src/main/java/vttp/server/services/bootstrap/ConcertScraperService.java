package vttp.server.services.bootstrap;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        Artist notFoundArtist = new Artist();
        notFoundArtist.setSpotifyId(NOT_FOUND_SPOTIFY_ID);
        notFoundArtist.setName(NOT_FOUND_NAME);
        notFoundArtist.setImageUrl640(PLACEHOLDER_640);
        notFoundArtist.setImageUrl320(PLACEHOLDER_320);
        notFoundArtist.setImageUrl160(PLACEHOLDER_160);
        artistRepo.upsertArtist(notFoundArtist);
        notFoundArtistId = artistRepo.getLastInsertId();
        logger.info("Initialized not_found artist with ID: %d".formatted(notFoundArtistId));

        // Run the scraper on startup
        scrapeUpcomingConcert();
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
    public void scrapeUpcomingConcert() {
        logger.info("Starting web scraping for upcoming concerts...");
        List<Object[]> concert_datesArg = new ArrayList<>();
        int page = 1;
        LocalDate today = LocalDate.now();
        boolean hasPastDate = false;
        Map<String, Long> artistIds = new HashMap<>();

        try {
            cleanupPastConcerts(today);

            while (!hasPastDate) {
                String currentUrl = String.format(BASE_URL, page);
                logger.info("Scraping page: %s".formatted(currentUrl));

                Document doc = Jsoup.connect(currentUrl)
                    .get();

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
                    System.out.println("artist name from setlist.fm: %s".formatted(artist));
                    LocalDate concertDate = LocalDate.parse(String.format("%s %s %s", day, month, year),
                            DATE_FORMATTER);
                    
                    // Normalize the strings
                    artist = encodingNormalization(artist);
                    tour = encodingNormalization(tour);
                    venue = encodingNormalization(venue);
                    country = encodingNormalization(country);

                    if (concertDate.isBefore(today)) {
                        hasPastDate = true;
                        break;
                    }

                    // Get artist spotify_id and image_urls from spotify API (if they are not in the hashmap)
                    Long artistId = artistIds.computeIfAbsent(artist, name -> {
                        Optional<Artist> artistOpt =spotifyApiSvc.getArtistSpotifyIdAndImage(name);
                        if (artistOpt.isPresent()) {
                            Artist spotifyArtist = artistOpt.get();
                            // Ensure that the first result returned is actually the 
                            // artist we are trying to get
                            System.out.println(name.toLowerCase());
                            System.out.println(spotifyArtist.getName().toLowerCase());
                            if (name.trim().equalsIgnoreCase(spotifyArtist.getName().trim())) {
                                artistRepo.upsertArtist(spotifyArtist);
                                return artistRepo.getLastInsertId();
                            }
                        }
                        return this.notFoundArtistId;
                    });
    
                    Thread.sleep(100);

                    // Check if concert exists
                    Long concertId;
                    Optional<Long> optId = concertRepo.getId(artist, venue, country);
                    // Don't exist => Insert new concert
                    if (optId.isEmpty()) {
                        concertRepo.insertConcert(artist, venue, country, tour, artistId);
                        concertId = concertRepo.getLastInsertId();
                    } else {
                        concertId = optId.get();
                    }

                    // add the concert Id to date
                    concert_datesArg.add(new Object[] { concertId, concertDate });

                }

                page++; // Move to next page
                Thread.sleep(Duration.ofSeconds(2));
            }

            // Batch insert all date_args
            if (!concert_datesArg.isEmpty()) {
                concertDateRepo.batchInsertDates(concert_datesArg);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            logger.warning("Error during scraping: %s".formatted(ex.getMessage()));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            logger.warning("Error while putting thread to sleep: %s".formatted(ex.getMessage()));
        }

    }

    private void cleanupPastConcerts(LocalDate today) {
        logger.info("Cleaning up past concert dates before %s".formatted(today));
        // Delete past dates
        concertDateRepo.deletePastDates(today);
        // Delete orphaned concerts
        concertRepo.deleteOrphanedConcerts();
    }

    // For characters that are non-standard
    private String encodingNormalization(String str) {
        return str.replaceAll("\\p{Pd}", "-")      // All dash punctuation (–, —, etc.) to -
            .replaceAll("[‘’`´]", "'")       // Single quotes/apostrophes to '
            .replaceAll("[“”„]", "\"")       // Double quotes to "
            .replaceAll("…", "...")          // Ellipsis to ...
            .replaceAll("[\\u00A0\\u200B]", " ") // Non-breaking/zero-width space to regular space
            .replaceAll("•", ".");           // Bullet to period
    }

}
