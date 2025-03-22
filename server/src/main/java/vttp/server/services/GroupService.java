package vttp.server.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private GroupMemberRepository memberRepo;

    @Autowired
    private ConcertRepository concertRepo;

    @Autowired
    private UserRepository userRepo;

    public Group createGroup(Group group) {
        // Validate that the concert exists
        if (!concertRepo.findById(group.getConcertId()).isPresent()) {
            throw new ResourceNotFoundException("Concert not found with id: " + group.getClass());
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

        // Return the created group with its ID
        group.setId(groupId);
        return group;
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

    // // Update members
    // public boolean requestToJoinGroup(Long groupId, Long userId) {
    //     // Check if the group exists
    //     Group group = getGroupById(groupId);

    //     // Check if the user exists
    //     userRepo.findById(userId)
    //             .orElseThrow(() -> new ResourceNotFoundException("User not found with id".formatted(userId)));

    //     // Check if user already has a pending/accepted request
    //     Optional<User> existingMembership = memberRepo.get

    //     // Add pending request
    //     return memberRepo.addMember(groupId, userId, "pending");
    // }

    // public boolean approveJoinRequest(Long groupId, Long userId, Long creatorId) {
    //     // Verify the approver is the creator
    //     // Update request status to 'accepted'
    //     return memberRepo.updateRequestStatus(groupId, userId, "accepted");
    // }

    // public boolean rejectJoinRequest(Long groupId, Long userId, Long creatorId) {
    //     // Verify the rejector is the creator
    //     // Update request status to 'rejected'
    //     return memberRepo.updateRequestStatus(groupId, userId, "rejected");
    // }

    // Update members
    public boolean joinGroup(Long groupId, Long userId) {
        // Check if the group exists
        Group group = getGroupById(groupId);

        // Check if the user exists
        userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id".formatted(userId)));

        // Check if the group has capacity
        if (group.getCapacity() != null) {
            int currentMembers = memberRepo.countMembers(groupId);
            if (currentMembers >= group.getCapacity()) {
                return false;
            }
        }

        // Add the user as a member
        return memberRepo.addMember(groupId, userId, "pending");
    }

    public boolean leaveGroup(Long groupId, Long userId) {
        return memberRepo.removeMember(groupId, userId);
    }

    public List<User> getGroupMembers(Long groupId) {
        // check if the group exists
        getGroupById(groupId); // throws exception if not found

        return memberRepo.getGroupMembers(groupId);
    }

}
