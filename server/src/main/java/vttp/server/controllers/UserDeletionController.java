package vttp.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.servlet.http.HttpServletRequest;
import vttp.server.services.UserDeletionService;

@RestController
@RequestMapping(path = "/api/protected")
public class UserDeletionController {

    @Autowired
    private UserDeletionService userDeletionSvc;

    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(HttpServletRequest httpRequest) {
        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));

        boolean deleted = userDeletionSvc.deleteUserAccount(userId);

        if (deleted) {
            return ResponseEntity.ok(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Account deleted successfully")
                    .build().toString());
        } else {
            return ResponseEntity.badRequest().body(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Failed to delete account")
                    .build().toString());
        }
    }
}
