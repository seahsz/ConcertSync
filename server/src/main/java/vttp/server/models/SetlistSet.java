package vttp.server.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class SetlistSet {

    private String name;
    private String encore; // To check if needed
    private List<SetlistSong> songs;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEncore() { return encore; }
    public void setEncore(String encore) { this.encore = encore; }
    
    public List<SetlistSong> getSongs() { return songs; }
    public void setSongs(List<SetlistSong> songs) { this.songs = songs; }

    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        if (name != null && !name.isEmpty())
            builder.add("name", name);
        else if (encore != null && !encore.isEmpty())
            builder.add("name", "encore + %s".formatted(encore));
        else
            builder.add("name", "");

        // Build the songs array
        if (songs != null && !songs.isEmpty()) {
            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
            for (SetlistSong song: songs)
                arrBuilder.add(song.toJson());
            builder.add("songs", arrBuilder.build());
        }
        return builder.build();
    }

    public Document toDocument() {
        Document doc = new Document();
        
        if (name != null && !name.isEmpty()) {
            doc.append("name", name);
        } else if (encore != null && !encore.isEmpty()) {
            doc.append("name", "encore " + encore);
        } else {
            doc.append("name", "");
        }

        if (songs != null && !songs.isEmpty()) {
            List<Document> songDocs = new ArrayList<>();
            for (SetlistSong song: songs)
                songDocs.add(song.toDocument());
            doc.append("songs", songDocs);
        }
        return doc;
    }
    
}
