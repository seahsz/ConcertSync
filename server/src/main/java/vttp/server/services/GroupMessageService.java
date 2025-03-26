package vttp.server.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vttp.server.exceptions.ResourceNotFoundException;
import vttp.server.models.GroupMessage;
import vttp.server.repositories.GroupMessageRepository;
import vttp.server.repositories.UserRepository;

@Service
public class GroupMessageService {

    @Autowired
    private GroupMessageRepository messageRepo;

    @Autowired UserRepository userRepo;

    @Autowired
    private GroupService groupSvc;

    public List<GroupMessage> getGroupMessages(Long groupId, int limit, int offset) {
        // CHeck if group exists
        groupSvc.getGroupById(groupId);

        // Get message from repo
        return messageRepo.getMessagesWithUserDetails(groupId, limit, offset);
    }

    public GroupMessage sendMessage(GroupMessage message) {
        // Check if group exists
        groupSvc.getGroupById(message.getGroupId());
        
        // Check if user exists
        userRepo.findById(message.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Add the message
        Long messageId = messageRepo.addMessage(message);
        
        // Retrieve the complete message with user details
        return messageRepo.getMessageById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Failed to retrieve created message"));
    }
    
}
