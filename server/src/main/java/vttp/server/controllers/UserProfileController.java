package vttp.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import vttp.server.models.angularDto.ProfileResponse;
import vttp.server.models.angularDto.ProfileUpdateRequest;
import vttp.server.services.UserProfileService;

// **Protected endpoint

// Successful -> returns updated Profile
// Unsuccessful -> User not found is caught in service layer -> handled by GlobalExceptionHandler
@RestController
@RequestMapping(path = "/api/protected", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileSvc;

    @GetMapping("/get-profile")
    public ResponseEntity<String> getProfile(HttpServletRequest request) {
        long id = Long.valueOf((String) request.getAttribute("id"));
        ProfileResponse profile = userProfileSvc.getProfile(id);
        return ResponseEntity.ok(profile.toJson().toString());
    }

    @PutMapping(path = "/update/phone-number", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updatePhoneNumber(@RequestBody ProfileUpdateRequest request,
            HttpServletRequest requestHttp) {
        long id = Long.valueOf((String) requestHttp.getAttribute("id"));
        userProfileSvc.updatePhoneNumber(id, request.getPhoneNumber());
        ProfileResponse profile = userProfileSvc.getProfile(id);
        return ResponseEntity.ok(profile.toJson().toString());
    }

    @PutMapping(path = "/update/name", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateName(@RequestBody ProfileUpdateRequest request,
            HttpServletRequest requestHttp) {
        long id = Long.valueOf((String) requestHttp.getAttribute("id"));
        userProfileSvc.updateName(id, request.getName());
        ProfileResponse profile = userProfileSvc.getProfile(id);
        return ResponseEntity.ok(profile.toJson().toString());
    }

    @PutMapping(path = "/update/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateProfilePicture(@RequestPart MultipartFile file,
            HttpServletRequest request) {
        long id = Long.valueOf((String) request.getAttribute("id"));
        userProfileSvc.updateProfilePicture(id, file);
        ProfileResponse profile = userProfileSvc.getProfile(id);
        return ResponseEntity.ok(profile.toJson().toString());
    }
}
