package vttp.server.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import vttp.server.exceptions.GroupLimitExceededException;
import vttp.server.exceptions.InvalidConcertDateException;
import vttp.server.exceptions.ResourceNotFoundException;
import vttp.server.models.Group;
import vttp.server.models.User;
import vttp.server.repositories.ConcertRepository;
import vttp.server.repositories.GroupMemberRepository;
import vttp.server.repositories.GroupRepository;
import vttp.server.repositories.UserRepository;

@Service
public class GroupService {

    @Value("${premium.group.limit}")
    private int premiumGroupLimit;

    @Value("${non.premium.group.limit}")
    private int nonPremiumGroupLimit;

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private GroupMemberRepository memberRepo;

    @Autowired
    private ConcertRepository concertRepo;

    @Autowired
    private UserRepository userRepo;

    public Group createGroup(Group group) {

        // Check user's premium status and groups created
        User creator = userRepo.findById(group.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));

        int groupsCreated = userRepo.getGroupsCreated(creator.getId());
        int groupLimit = creator.isPremiumStatus() ? premiumGroupLimit : nonPremiumGroupLimit;

        if (groupsCreated >= groupLimit) {
            throw new GroupLimitExceededException("You have reached your group creation limit");
        }

        // Validate that the concert exists
        if (!concertRepo.findById(group.getConcertId()).isPresent()) {
            throw new ResourceNotFoundException("Concert not found with id: " + group.getConcertId());
        }

        // Validate that the concert date is valid for this concert
        if (!groupRepo.isValidConcertDate(group.getConcertId(), group.getConcertDate())) {
            throw new InvalidConcertDateException("Invalid concert date for concert id: %s".formatted(
                    group.getConcertId()));
        }

        // Insert the group
        Long groupId = groupRepo.insertGroup(group);

        // Add the creator as a member
        memberRepo.addMember(groupId, group.getCreatorId(), "accepted");

        // Update the user's group count
        userRepo.incrementGroupsCreated(group.getCreatorId());

        // Retrieve the complete group from the database
        return groupRepo.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Failed to retrieve created group"));

    }

    public List<Group> getGroupsByConcert(Long concertId) {
        return groupRepo.findByConcertId(concertId);
    }

    public List<Group> getGroupsByUser(Long userId) {
        return memberRepo.getUserGroup(userId);
    }

    public Group getGroupById(Long groupId) {
        return groupRepo.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: %s".formatted(groupId)));
    }

    // Update members
    public boolean requestToJoinGroup(Long groupId, Long userId) {
        // Check if the group exists
        Group group = getGroupById(groupId);

        // Check if the user exists
        userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id".formatted(userId)));

        // Check if user already has a request (pending/accepted/rejected) - if
        // rejected, users can't apply again
        if (memberRepo.isMember(groupId, userId))
            return false;

        // Check if the group has capacity
        if (group.getCapacity() != null) {
            int currentMembers = memberRepo.countMembers(groupId);
            if (currentMembers >= group.getCapacity()) {
                return false;
            }
        }

        // Add pending request
        return memberRepo.addMember(groupId, userId, "pending");
    }

    public boolean approveJoinRequest(Long groupId, Long userId, Long creatorId) {
        // Get the group to verify the creator
        Group group = getGroupById(groupId);

        // Verify the approver is the creator
        if (!group.getCreatorId().equals(creatorId)) {
            return false; // Only the creator can approve requests
        }

        // Check if the user has a pending request
        if (!memberRepo.isMember(groupId, userId)) {
            return false; // No request to approve
        }

        // Update request status to 'accepted'
        return memberRepo.updateRequestStatus(groupId, userId, "accepted");
    }

    public boolean rejectJoinRequest(Long groupId, Long userId, Long creatorId) {
        // Get the group to verify the creator
        Group group = getGroupById(groupId);

        // Verify the rejector is the creator
        if (!group.getCreatorId().equals(creatorId)) {
            return false; // Only the creator can reject requests
        }

        // Check if the user has a pending request
        if (!memberRepo.isMember(groupId, userId)) {
            return false; // No request to reject
        }

        // Update request status to 'rejected'
        return memberRepo.updateRequestStatus(groupId, userId, "rejected");
    }

    public boolean leaveGroup(Long groupId, Long userId) {
        return memberRepo.removeMember(groupId, userId);
    }

    public List<User> getGroupMembers(Long groupId) {
        // check if the group exists
        getGroupById(groupId); // throws exception if not found

        return memberRepo.getGroupMembers(groupId);
    }

    public List<User> getPendingJoinRequests(Long groupId) {
        // Check if the group exists
        getGroupById(groupId);

        return memberRepo.getPendingRequests(groupId);
    }

    // New methods for chat functionality
    public String getMembershipStatus(Long groupId, Long userId) {
        // Check if the user is the creator
        Group group = getGroupById(groupId);
        if (group.getCreatorId().equals(userId)) {
            return "creator";
        }

        // Check if the user is a member
        return memberRepo.getMembershipStatus(groupId, userId);
    }

    public boolean isGroupMember(Long groupId, Long userId) {
        String status = getMembershipStatus(groupId, userId);
        return "creator".equals(status) || "accepted".equals(status);
    }

}
