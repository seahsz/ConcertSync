package vttp.server.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Setlist {

   private String id;
    private String artistName;
    private String venueName;
    private String cityName;
    private String countryName;
    private String tourName;
    private String eventDate;
    private List<SetlistSet> sets;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public List<SetlistSet> getSets() { return sets; }
    public void setSets(List<SetlistSet> sets) { this.sets = sets; }

    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("id", id != null ? id : "")
            .add("artistName", artistName)
            .add("venueName", venueName)
            .add("cityName", cityName)
            .add("countryName", countryName)
            .add("eventDate", eventDate);

        if (tourName != null && !tourName.isEmpty())
            builder.add("tourName", tourName);

        if (sets != null && !sets.isEmpty()) {
            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
            for (SetlistSet set: sets)
                arrBuilder.add(set.toJson());
            builder.add("sets", arrBuilder.build());
        }
        return builder.build();
    }

    public Document toDocument() {
        Document doc = new Document()
            .append("id", id != null ? id : "")
            .append("artistName", artistName)
            .append("venueName", venueName)
            .append("cityName", cityName)
            .append("countryName", countryName)
            .append("eventDate", eventDate);

        if (sets != null && !sets.isEmpty()) {
            List<Document> setsDocuments = new ArrayList<>();
            for (SetlistSet set : sets) {
                setsDocuments.add(set.toDocument());
            }
            doc.append("sets", setsDocuments);
        }
        return doc;
    }
    
}
