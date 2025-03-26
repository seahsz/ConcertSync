package vttp.server.services;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import vttp.server.models.Group;
import vttp.server.models.User;
import vttp.server.repositories.DeletedUsersRepository;
import vttp.server.repositories.GroupMemberRepository;
import vttp.server.repositories.GroupRepository;
import vttp.server.repositories.UserRepository;

@Service
public class UserDeletionService {

    private static final Logger logger = Logger.getLogger(UserDeletionService.class.getName());

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private GroupMemberRepository groupMemberRepo;
    
    @Autowired
    private DeletedUsersRepository deletedUsersRepo;
    
    @Autowired
    private S3Service s3Service;

    @Transactional
    public boolean deleteUserAccount(Long userId) {
        try {
            // Check if user exists
            Optional<User> userOpt = userRepo.findById(userId);
            if (userOpt.isEmpty()) {
                logger.warning("Cannot delete non-existent user with ID: " + userId);
                return false;
            }
            
            User user = userOpt.get();
            
            // Get list of groups the user has joined
            List<Group> userGroups = groupMemberRepo.getUserGroup(userId);
            
            // Create JSON array of group IDs
            JsonArrayBuilder groupIdsBuilder = Json.createArrayBuilder();
            for (Group group : userGroups) {
                groupIdsBuilder.add(group.getId());
            }
            String groupsJson = groupIdsBuilder.build().toString();
            
            // Store user info in deleted_users table
            deletedUsersRepo.recordDeletedUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getBirthDate(),
                user.getPhoneNumber(),
                groupsJson
            );
            
            // Handle profile picture deletion
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().startsWith("/images/")) {
                // Delete the profile picture from S3 if it's not a default image
                try {
                    String fileName = extractFileNameFromUrl(user.getProfilePictureUrl());
                    s3Service.deleteFile(fileName);
                    logger.info("Deleted profile picture: " + fileName);
                } catch (Exception e) {
                    logger.warning("Failed to delete profile picture for user " + userId + ": " + e.getMessage());
                    // Continue with deletion even if profile picture deletion fails
                }
            }
            
            // Check for user-created groups (ON DELETE RESTRICT constraint)
            List<Group> createdGroups = groupRepo.findByCreatorId(userId);
            if (!createdGroups.isEmpty()) {
                // Handle groups created by the user
                for (Group group : createdGroups) {
                    // For each group, find a new owner or delete the group
                    handleGroupOwnership(group);
                }
            }
            
            // Now we can delete the user (group_members and group_messages will cascade)
            return userRepo.deleteUser(userId);
            
        } catch (Exception e) {
            logger.severe("Error during user deletion process: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Handle group ownership when deleting a user who is a creator
     * 
     * @param group The group to handle
     */
    private void handleGroupOwnership(Group group) {
        // Get all member IDs for the group
        List<Long> groupMembers = groupRepo.getGroupMemberIds(group.getId());
        
        // Remove the creator from the list of potential new owners
        groupMembers.removeIf(id -> id.equals(group.getCreatorId()));
        
        if (groupMembers.isEmpty()) {
            // No other members, delete the group
            logger.info("Deleting group with ID: " + group.getId() + " as it has no other members");
            groupRepo.deleteGroup(group.getId());
        } else {
            // Transfer ownership to the first member
            Long newOwnerId = groupMembers.get(0);
            logger.info("Transferring ownership of group ID: " + group.getId() + 
                        " from user ID: " + group.getCreatorId() + " to user ID: " + newOwnerId);
            groupRepo.updateGroupCreator(group.getId(), newOwnerId);
        }
    }
    
    // private utility method
    private String extractFileNameFromUrl(String url) {
        int startIndex = url.lastIndexOf("/") + 1;
        return url.substring(startIndex);
    }
    
}
