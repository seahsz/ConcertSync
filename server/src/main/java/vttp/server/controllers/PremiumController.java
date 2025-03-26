package vttp.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import vttp.server.exceptions.NotAuthenticatedException;
import vttp.server.exceptions.NotPremiumException;
import vttp.server.models.User;
import vttp.server.repositories.UserRepository;

@RestController
@RequestMapping(path = "/api/premium")
public class PremiumController {

    @Autowired
    private UserRepository userRepo;
    
    @GetMapping("/test")
    public ResponseEntity<String> premiumTest(HttpServletRequest request) {
        String id = (String) request.getAttribute("id");
        if (id ==  null) {
            throw new NotAuthenticatedException();
        }
        // Check if user exists and has premium status
        User user = userRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new NotAuthenticatedException());
        if (!user.isPremiumStatus()) {
            throw new NotPremiumException();
        }
        return ResponseEntity.ok("Premium endpoint accessed by " + id);
    }
}
