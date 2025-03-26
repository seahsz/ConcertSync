package vttp.server.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import vttp.server.models.angularDto.Concert;
import vttp.server.services.ConcertService;

@RestController
@RequestMapping("/api/concerts")
public class ConcertController {

    Logger logger = Logger.getLogger(ConcertController.class.getName());

    @Autowired
    private ConcertService concertSvc;

    @GetMapping(path = "/upcoming", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUpcomingConcerts() {
        try {
            List<Concert> concerts = concertSvc.getUpcomingConcerts(LocalDate.now());

            // Build JSON Manually
            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
            for (Concert concert : concerts) {
                JsonObjectBuilder objBuilder = Json.createObjectBuilder()
                        .add("id", concert.getId())
                        .add("artist", concert.getArtist())
                        .add("venue", concert.getVenue())
                        .add("country", concert.getCountry())
                        .add("tour", concert.getTour())
                        .add("image_url_320", concert.getImageUrl320());
                JsonArrayBuilder dateBuilder = Json.createArrayBuilder();
                for (LocalDate date : concert.getDates()) {
                    dateBuilder.add(date.toString());
                }
                objBuilder.add("dates", dateBuilder.build());
                arrBuilder.add(objBuilder.build());
            }
            logger.info("Returning %d upcoming concerts".formatted(concerts.size()));
            return ResponseEntity.ok(arrBuilder.build().toString());
        } catch (Exception e) {
            logger.severe("Error fetching concets: %s".formatted(e.getMessage()));
            String errorJson = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build()
                    .toString();
            return ResponseEntity.status(500).body(errorJson);
        }
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getConcertById(@PathVariable Long id) {
        try {
            Optional<Concert> concertOpt = concertSvc.getConcertById(id);

            if (concertOpt.isEmpty()) {
                String errorJson = Json.createObjectBuilder()
                        .add("error", "Concert not found with ID: " + id)
                        .build()
                        .toString();
                return ResponseEntity.status(404).body(errorJson);
            }

            Concert concert = concertOpt.get();

            // Build JSON Manually
            JsonObjectBuilder objBuilder = Json.createObjectBuilder()
                    .add("id", concert.getId())
                    .add("artist", concert.getArtist())
                    .add("venue", concert.getVenue())
                    .add("country", concert.getCountry())
                    .add("tour", concert.getTour())
                    .add("image_url_320", concert.getImageUrl320());

            JsonArrayBuilder dateBuilder = Json.createArrayBuilder();
            for (LocalDate date : concert.getDates()) {
                dateBuilder.add(date.toString());
            }
            objBuilder.add("dates", dateBuilder.build());

            logger.info("Returning concert details for ID: " + id);
            return ResponseEntity.ok(objBuilder.build().toString());
        } catch (Exception e) {
            logger.severe("Error fetching concert with ID " + id + ": " + e.getMessage());
            String errorJson = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build()
                    .toString();
            return ResponseEntity.status(500).body(errorJson);
        }
    }

}
