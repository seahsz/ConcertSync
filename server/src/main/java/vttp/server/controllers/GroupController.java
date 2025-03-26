package vttp.server.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import vttp.server.models.Group;
import vttp.server.models.GroupMessage;
import vttp.server.models.User;
import vttp.server.models.angularDto.CreateGroupRequest;
import vttp.server.services.GroupMessageService;
import vttp.server.services.GroupService;

@RestController
@RequestMapping("/api/protected/groups")
public class GroupController {
    @Autowired
    private GroupService groupSvc;

    @Autowired
    private GroupMessageService messageSvc;

    @PostMapping
    public ResponseEntity<String> createGroup(@RequestBody CreateGroupRequest request,
            HttpServletRequest requestHttp) {

        long userId = Long.valueOf((String) requestHttp.getAttribute("id"));

        // Map request to group model
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setConcertId(request.getConcertId());
        group.setConcertDate(request.getConcertDate());
        group.setCreatorId(Long.valueOf(userId));
        group.setCapacity(request.getCapacity());
        group.setPublic(request.isPublic());

        // Create the group
        Group createdGroup = groupSvc.createGroup(group);

        // Return response
        JsonObject response = Json.createObjectBuilder()
                .add("success", true)
                .add("message", "Group created successfully")
                .add("group", createdGroup.toJson())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response.toString());
    }

    @GetMapping("/concert/{concertId}")
    public ResponseEntity<String> getGroupsByConcert(@PathVariable Long concertId) {
        List<Group> groups = groupSvc.getGroupsByConcert(concertId);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Group g : groups) {
            arrayBuilder.add(g.toJson());
        }

        JsonObject response = Json.createObjectBuilder()
                .add("groups", arrayBuilder.build())
                .build();

        return ResponseEntity.ok(response.toString());
    }

    @GetMapping("/my-groups")
    public ResponseEntity<String> getMyGroups(HttpServletRequest httpRequest) {
        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));

        List<Group> groups = groupSvc.getGroupsByUser(userId);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Group g : groups) {
            arrayBuilder.add(g.toJson());
        }

        JsonObject response = Json.createObjectBuilder()
                .add("groups", arrayBuilder.build())
                .build();

        return ResponseEntity.ok(response.toString());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<String> getGroupDetails(@PathVariable Long groupId) {
        Group group = groupSvc.getGroupById(groupId);
        List<User> members = groupSvc.getGroupMembers(groupId);

        JsonArrayBuilder membersArray = Json.createArrayBuilder();
        for (User member : members) {
            membersArray.add(member.toJson());
        }

        JsonObject response = Json.createObjectBuilder()
                .add("group", group.toJson())
                .add("members", membersArray.build())
                .add("memberCount", members.size())
                .build();

        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/{groupId}/request")
    public ResponseEntity<String> requestToJoinGroup(@PathVariable Long groupId, HttpServletRequest httpRequest) {
        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));

        boolean joined = groupSvc.requestToJoinGroup(groupId, userId);

        JsonObject response = Json.createObjectBuilder()
                .add("success", joined)
                .add("message", joined ? "Join request submitted" : "Failed to submit join request")
                .build();

        return joined ? ResponseEntity.ok(response.toString())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toString());
    }

    @PostMapping("/{groupId}/approve/{userId}")
    public ResponseEntity<String> approveJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {

        long creatorId = Long.valueOf((String) httpRequest.getAttribute("id"));

        boolean approved = groupSvc.approveJoinRequest(groupId, userId, creatorId);

        JsonObject response = Json.createObjectBuilder()
                .add("success", approved)
                .add("message", approved ? "Join request approved" : "Failed to approve join request")
                .build();

        return approved ? ResponseEntity.ok(response.toString())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toString());
    }

    @PostMapping("/{groupId}/reject/{userId}")
    public ResponseEntity<String> rejectJoinRequest(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {

        long creatorId = Long.valueOf((String) httpRequest.getAttribute("id"));

        boolean rejected = groupSvc.rejectJoinRequest(groupId, userId, creatorId);

        JsonObject response = Json.createObjectBuilder()
                .add("success", rejected)
                .add("message", rejected ? "Join request rejected" : "Failed to reject join request")
                .build();

        return rejected ? ResponseEntity.ok(response.toString())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toString());
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<String> leaveGroup(@PathVariable Long groupId, HttpServletRequest httpRequest) {
        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));

        boolean left = groupSvc.leaveGroup(groupId, userId);

        JsonObject response = Json.createObjectBuilder()
                .add("success", left)
                .add("message", left ? "Successfully left group" : "Failed to leave group")
                .build();

        return left ? ResponseEntity.ok(response.toString())
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toString());
    }

    @GetMapping("/{groupId}/pending-requests")
    public ResponseEntity<String> getPendingRequests(
            @PathVariable Long groupId,
            HttpServletRequest httpRequest) {

        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));

        // Get the group
        Group group = groupSvc.getGroupById(groupId);

        // Verify the requester is the creator
        if (!group.getCreatorId().equals(userId)) {
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Only the creator can view pending requests")
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse.toString());
        }

        List<User> pendingUsers = groupSvc.getPendingJoinRequests(groupId);

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (User user : pendingUsers) {
            arrayBuilder.add(user.toJson());
        }

        JsonObject response = Json.createObjectBuilder()
                .add("pendingRequests", arrayBuilder.build())
                .build();

        return ResponseEntity.ok(response.toString());
    }

    @GetMapping("/{groupId}/membership-status")
    public ResponseEntity<String> getMembershipStatus(
            @PathVariable Long groupId,
            HttpServletRequest httpRequest) {

        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));
        String status = groupSvc.getMembershipStatus(groupId, userId);

        JsonObject response = Json.createObjectBuilder()
                .add("status", status)
                .build();

        return ResponseEntity.ok(response.toString());
    }

    @GetMapping("/{groupId}/messages")
    public ResponseEntity<String> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest httpRequest) {
        
        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));
        
        // Check if user is a member of the group
        if (!groupSvc.isGroupMember(groupId, userId)) {
            JsonObject response = Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "You must be a member of this group to view messages")
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.toString());
        }
        
        // Get messages with user details
        List<GroupMessage> messages = messageSvc.getGroupMessages(groupId, limit, offset);
        
        JsonArrayBuilder messagesArray = Json.createArrayBuilder();
        for (GroupMessage message : messages) {
            messagesArray.add(message.toJson());
        }
        
        JsonObject response = Json.createObjectBuilder()
                .add("messages", messagesArray.build())
                .build();
        
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/{groupId}/messages")
    public ResponseEntity<String> sendMessage(
            @PathVariable Long groupId,
            @RequestBody Map<String, String> messageRequest,
            HttpServletRequest httpRequest) {
        
        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));
        
        // Check if user is a member of the group
        if (!groupSvc.isGroupMember(groupId, userId)) {
            JsonObject response = Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "You must be a member of this group to send messages")
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response.toString());
        }

        // Extract message content from the request
        String messageContent = messageRequest.get("message");
        
        // Create and save message
        GroupMessage newMessage = new GroupMessage();
        newMessage.setGroupId(groupId);
        newMessage.setUserId(userId);
        newMessage.setMessage(messageContent);
        
        JsonObject savedMessage = messageSvc.sendMessage(newMessage).toJson();
        
        JsonObject response = Json.createObjectBuilder()
                .add("success", true)
                .add("message", "Message sent successfully")
                .add("message", savedMessage)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response.toString());
    }

}