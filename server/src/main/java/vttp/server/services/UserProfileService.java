package vttp.server.services;

import java.time.LocalDate;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vttp.server.exceptions.NameUpdateFrequencyException;
import vttp.server.exceptions.ProfilePictureUploadException;
import vttp.server.exceptions.UserNotFoundException;
import vttp.server.models.User;
import vttp.server.models.angularDto.ProfileResponse;
import vttp.server.repositories.UserRepository;

@Service
public class UserProfileService {

    private Logger logger = Logger.getLogger(UserProfileService.class.getName());

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private S3Service s3Svc;

    private final static long nameUpdateRestrictionPeriod = 0;

    public ProfileResponse getProfile(long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return new ProfileResponse(user);
    }

    public void updatePhoneNumber(long id, String newPhoneNum) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setPhoneNumber(newPhoneNum);
        userRepo.updatePhoneNumber(id, newPhoneNum);
    }

    public void updateName(long id, String newName) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (hasRecentlyUpdatedName(user))
            throw new NameUpdateFrequencyException("You can only update your name once every 30 days");
        userRepo.updateName(id, newName, LocalDate.now());
    }

    // Private utility method
    private boolean hasRecentlyUpdatedName(User user) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(nameUpdateRestrictionPeriod);
        return user.getLastNameUpdate().isAfter(thirtyDaysAgo);
    }

    public void updateProfilePicture(long id, MultipartFile file) {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        if (user.getProfilePictureUrl() != null) {
            // .... delete old file
            String fileName = extractFileNameFromUrl(user.getProfilePictureUrl());
            s3Svc.deleteFile(fileName);
        }
        try {
            String newUrl = s3Svc.uploadFile(file.getBytes(), file.getOriginalFilename());
            userRepo.updateProfilePicture(id, newUrl);
        } catch (Exception ex) {
            logger.warning("Error updating profile picture for id %d: %s".formatted(id, ex.getMessage()));
            throw new ProfilePictureUploadException(id, ex.getMessage());
        }
    }

    // private utility method
    private String extractFileNameFromUrl(String url) {
        int startIndex = url.lastIndexOf("/") + 1;
        return url.substring(startIndex);
    }
    
}
