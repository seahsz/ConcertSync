package vttp.server.controllers;

import java.util.List;
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
import vttp.server.models.Setlist;
import vttp.server.services.bootstrap.SetlistApiService;

@RestController
@RequestMapping("/api/setlists")
public class SetlistController {
    
    private static final Logger logger = Logger.getLogger(SetlistController.class.getName());

    @Autowired
    private SetlistApiService setlistApiSvc;

    @GetMapping(produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllSetLists() {
        List<Setlist> setlists = setlistApiSvc.getAllSetlists();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Setlist setlist : setlists)
            arrayBuilder.add(setlist.toJson());

        logger.info("Returning %s setlists".formatted(setlists.size()));
        return ResponseEntity.ok(arrayBuilder.build().toString());
    }

    @GetMapping(path = "/artist/{artistName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSetlistsByArtist(@PathVariable String artistName) {
        try {
            List<Setlist> setlists = setlistApiSvc.getSetlistsForArtist(artistName);
            
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Setlist setlist : setlists) {
                arrayBuilder.add(setlist.toJson());
            }
            
            logger.info("Returning setlists for artist: " + artistName + ", count: " + setlists.size());
            return ResponseEntity.ok(arrayBuilder.build().toString());
        } catch (Exception e) {
            logger.severe("Error fetching setlists for artist " + artistName + ": " + e.getMessage());
            String errorJson = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build()
                    .toString();
            return ResponseEntity.status(500).body(errorJson);
        }
    }
}
