package vttp.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vttp.server.services.StripeService;

@RestController
@RequestMapping("/subscription/webhook/stripe")
public class WebhookController {
    
    @Autowired
    private StripeService stripeSvc;

    @PostMapping()
    public ResponseEntity<String> handleStripWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader) {

        stripeSvc.handleWebhookEvent(payload, sigHeader);
        return ResponseEntity.ok("Webhook processed");    
    }
}
