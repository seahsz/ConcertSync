package vttp.server.services.bootstrap;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import vttp.server.models.Setlist;
import vttp.server.repositories.ArtistRepository;

@Service
public class SetlistSyncService {

    private static final Logger logger = Logger.getLogger(SetlistSyncService.class.getName());

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private SetlistApiService setlistApiService;

    @PostConstruct
    public void initializeSetlists() {
        logger.info("Starting initial setlist synchronization for all artists...");
        syncSetlistsForAllArtists();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledSetlistSync() {
        logger.info("Starting scheduled setlist synchronization for all artists...");
        syncSetlistsForAllArtists();
    }

    private void syncSetlistsForAllArtists() {
        try {
            // Get all artist names from the repository
            List<String> artistNames = artistRepository.getAllArtistNames();

            logger.info("Found " + artistNames.size() + " artists to synchronize setlists");

            int successCount = 0;
            int errorCount = 0;

            // Process each artist
            for (String artistName : artistNames) {
                try {
                    logger.info("Syncing setlists for artist: " + artistName);

                    // Use the existing service to get/update setlists
                    List<Setlist> setlists = setlistApiService.getSetlistsForArtistForSync(artistName);

                    logger.info("Successfully synced " + setlists.size() + " setlists for artist: " + artistName);
                    successCount++;

                    // Add a delay to avoid hitting rate limits
                    Thread.sleep(Duration.ofSeconds(3).toMillis());
                } catch (Exception e) {
                    logger.warning("Error syncing setlists for artist: %s. %s".formatted(artistName, e.getMessage()));
                    errorCount++;

                    // Continue with the next artist even if one fails
                    continue;
                }
            }

            logger.info("Setlist synchronization completed. Success: " + successCount + ", Errors: " + errorCount);
        } catch (Exception e) {
            logger.severe("Error during setlist synchronization process: %s".formatted(e.getMessage()));
        }
    }

}
