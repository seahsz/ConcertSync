package vttp.server.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import vttp.server.models.Group;
import vttp.server.models.User;
import vttp.server.models.angularDto.CreateGroupRequest;
import vttp.server.services.GroupService;

@RestController
@RequestMapping("/api/protected/groups")
public class GroupController {

    @Autowired
    private GroupService groupSvc;

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
        for (Group g: groups) {
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
        for (Group g: groups) {
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
        for (User member: members) {
            membersArray.add(member.toJson());
        }

        JsonObject response = Json.createObjectBuilder()
            .add("group", group.toJson())
            .add("members", membersArray.build())
            .add("memberCount", members.size())
            .build();
        
        return ResponseEntity.ok(response.toString());
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<String> joinGroup(@PathVariable Long groupId, HttpServletRequest httpRequest) {
        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));

        boolean joined = groupSvc.joinGroup(groupId, userId);

        JsonObject response = Json.createObjectBuilder()
            .add("success", joined)
            .add("message", joined ? "Successfully joined group" : "Failed to join group")
            .build();
        
        return joined ?
            ResponseEntity.ok(response.toString()) :
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toString());
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<String> leaveGroup(@PathVariable Long groupId, HttpServletRequest httpRequest) {
        long userId = Long.valueOf((String) httpRequest.getAttribute("id"));

        boolean left = groupSvc.leaveGroup(groupId, userId);

        JsonObject response = Json.createObjectBuilder()
            .add("success", left)
            .add("message", left ? "Successfully left group" : "Failed to leave group")
            .build();
        
        return left ?
            ResponseEntity.ok(response.toString()) :
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.toString());
    }
    
}
