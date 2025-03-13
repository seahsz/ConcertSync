package vttp.server.models.angularDto;

import java.util.HashMap;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class ErrorResponse {

    private boolean success = false;
    private Map<String, Boolean> errors = new HashMap<>();

    public ErrorResponse() {}

    public boolean isSuccess() { return success; }
    public Map<String, Boolean> getErrors() { return errors; }

    public JsonObject toJson() {
        System.out.println(getErrors().size());
        JsonObjectBuilder objBuilder = Json.createObjectBuilder();

        for (Map.Entry<String, Boolean> entry: getErrors().entrySet()) {
            objBuilder.add(entry.getKey(), entry.getValue());
        }

        return Json.createObjectBuilder()
                .add("success", isSuccess())
                .add("errors", objBuilder.build())
                .build();
    }
    
}
