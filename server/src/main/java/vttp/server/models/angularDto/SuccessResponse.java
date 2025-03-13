package vttp.server.models.angularDto;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class SuccessResponse {

    private boolean success = true;
    private String message;
    private String token; // Optional. Used for login

    public SuccessResponse(String message) {
        this.message = message;
    }

    public SuccessResponse(String mesage, String token) {
        this.message = mesage;
        this.token = token;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }

    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("success", isSuccess())
                .add("message", getMessage());
        
        if (token != null)
            builder.add("token", token);
        return builder.build();
    }
    
}
