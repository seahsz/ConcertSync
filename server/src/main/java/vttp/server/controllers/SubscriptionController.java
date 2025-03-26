package vttp.server.controllers;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.servlet.http.HttpServletRequest;
import vttp.server.services.StripeService;

@RestController
@RequestMapping(path = "/api/protected/subscription")
public class SubscriptionController {

    @Autowired
    private StripeService stripeSvc;

    Logger logger = Logger.getLogger(SubscriptionController.class.getName());

    @PostMapping("/create-checkout")
    public ResponseEntity<String> createCheckoutSession(HttpServletRequest httpRequest) {
        Long userId = Long.valueOf((String) httpRequest.getAttribute("id"));
        String checkoutUrl = stripeSvc.createCheckoutSession(userId);

        return ResponseEntity.ok(Json.createObjectBuilder()
                .add("success", true)
                .add("url", checkoutUrl)
                .build().toString());
    }

    @PostMapping("/reactivate")
    public ResponseEntity<String> reactivateSubscription(HttpServletRequest request) {
        try {
            Long userId = Long.valueOf((String) request.getAttribute("id"));
            boolean success = stripeSvc.reactivateSubscription(userId);
            
            String response = Json.createObjectBuilder()
                .add("success", success)
                .add("message", success ? "Subscription reactivated successfully" : "Failed to reactivate subscription")
                .build()
                .toString();
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error reactivating subscription: " + e.getMessage());
            
            String response = Json.createObjectBuilder()
                .add("success", false)
                .add("error", e.getMessage())
                .build()
                .toString();
                
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelSubscription(HttpServletRequest httpRequest) {
        Long userId = Long.valueOf((String) httpRequest.getAttribute("id"));
        boolean success = stripeSvc.cancelSubscription(userId);
        
        return ResponseEntity.ok(Json.createObjectBuilder()
                .add("success", success)
                .add("message", success ? "Subscription canceled" : "Failed to cancel subscription")
                .build().toString());
    }
    
}
