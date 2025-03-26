package vttp.server.models;

import org.bson.Document;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class SetlistSong {

    private String name;
    private String info;
    private Boolean tape;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }
    
    public Boolean getTape() { return tape; }
    public void setTape(Boolean tape) { this.tape = tape; }

    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
            .add("name", name)
            .add("tape", tape);

        if (info != null && !info.isEmpty())
            builder.add("info", info);

        return builder.build();
    }
    
    public Document toDocument() {
        Document doc = new Document()
            .append("name", name)
            .append("tape", tape);
            
        if (info != null && !info.isEmpty())
            doc.append("info", info);

        return doc;
    }
    
}
