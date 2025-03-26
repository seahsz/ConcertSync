package vttp.server.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;
import vttp.server.models.User;
import vttp.server.repositories.UserRepository;

@Service
public class StripeService {

    private Logger logger = Logger.getLogger(StripeService.class.getName());

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    @Value("${subscription.price.id}")
    private String subscriptionPriceId;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Autowired
    private UserRepository userRepo;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    // Create checkout session
    public String createCheckoutSession(Long userId) {
        try {
            User user = userRepo.findById(userId).orElseThrow();

            SessionCreateParams params = new SessionCreateParams.Builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(user.getEmail())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(subscriptionPriceId)
                            .setQuantity(1L)
                            .build())
                    .putMetadata("userId", userId.toString())
                    .build();

            Session session = Session.create(params);
            return session.getUrl();

        } catch (Exception e) {
            throw new RuntimeException("Error creating checkout session", e);
        }
    }

    // Renew subscription
    public boolean reactivateSubscription(Long userId) {
        try {
            User user = userRepo.findById(userId).orElseThrow();

            if (user.getSubscriptionId() == null) {
                logger.warning("User %s has no subscription to reactive".formatted(userId));
                return false;
            }

            Subscription subscription = Subscription.retrieve(user.getSubscriptionId());

            if (subscription.getCancelAtPeriodEnd()) {
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(false)
                        .build();

                subscription.update(params);

                // Update user record in database
                user.setAutoRenew(true);
                userRepo.updateSubscription(user.getId(), user.isPremiumStatus(),
                        user.getPremiumExpiry(), user.getSubscriptionId(), true);

                logger.info("Subscription reactivated for user " + userId);
                return true;
            } else {
                // Subscription isn't scheduled for cancellation
                logger.info("Subscription for user " + userId + " is not scheduled for cancellation");
                return false;
            }
        } catch (Exception e) {
            logger.severe("Error reactivating subscription: " + e.getMessage());
            return false;
        }
    }

    // Cancel subscription
    public boolean cancelSubscription(Long userId) {
        try {
            User user = userRepo.findById(userId).orElseThrow();

            if (user.getSubscriptionId() == null) {
                logger.warning("User %s has no active subscription to cancel".formatted(userId));
                return false;
            }

            Subscription subscription = Subscription.retrieve(user.getSubscriptionId());

            // Set subscription to cancel at period end
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();

            subscription.update(params);

            // Update user record in database
            user.setAutoRenew(false);
            userRepo.updateSubscription(user.getId(), user.isPremiumStatus(),
                    user.getPremiumExpiry(), user.getSubscriptionId(), false);

            logger.info("Subscription set to cancel for user " + userId);
            return true;

        } catch (Exception e) {
            logger.severe("Error canceling subscription: " + e.getMessage());
            return false;
        }
    }

    // Webhook
    public void handleWebhookEvent(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);

            // Handle the event
            switch (event.getType()) {
                case "checkout.session.completed":
                    handleCheckoutSessionCompleted(event);
                    break;

                case "customer.subscription.updated":
                    handleSubscriptionUpdated(event);
                    break;

                case "customer.subscription.deleted":
                    handleSubscriptionCanceled(event);
                    break;

                default:
                    logger.info("Unhandled event type: " + event.getType());

            }

        } catch (Exception e) {
            logger.severe("Error handling webhook: " + e.getMessage());
            throw new RuntimeException("Webhook error: " + e.getMessage(), e);
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        logger.info("In checkout session webhook handler");
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObj = dataObjectDeserializer.getObject().orElseThrow();
        Session session = (Session) stripeObj;

        String userId = session.getMetadata().get("userId");
        String subscriptionId = session.getSubscription();

        if (userId != null && subscriptionId != null) {
            try {
                // Retrieve the subscription to get the current period end
                Subscription subscription = Subscription.retrieve(subscriptionId);

                // Convert timestamp to LocalDate
                LocalDate expiryDate = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                User user = userRepo.findById(Long.valueOf(userId)).orElseThrow();
                user.setPremiumStatus(true);
                user.setPremiumExpiry(expiryDate);
                user.setSubscriptionId(subscriptionId);
                user.setAutoRenew(true);

                // Update the user in the database
                userRepo.updateSubscription(user.getId(), true, expiryDate, subscriptionId, true);

                logger.info("Premium subscription activated for user: " + userId);

            } catch (Exception e) {
                logger.severe("Error processing checkout session: " + e.getMessage());
            }
        }
    }

    private void handleSubscriptionUpdated(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObj = dataObjectDeserializer.getObject().orElseThrow();
        Subscription subscription = (Subscription) stripeObj;

        String subscriptionId = subscription.getId();

        try {
            // Find user with this subscriptionId
            Optional<User> userOpt = userRepo.findBySubscriptionId(subscriptionId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                LocalDate expiryDate = Instant.ofEpochSecond(subscription.getCurrentPeriodEnd())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                // Update the expiry date
                user.setPremiumExpiry(expiryDate);

                // Check if subscription is still active
                boolean isActive = subscription.getStatus().equals("active");
                user.setPremiumStatus(isActive);

                // Check cancellation info
                boolean willCancel = subscription.getCancelAtPeriodEnd() != null && subscription.getCancelAtPeriodEnd();
                user.setAutoRenew(!willCancel);

                // Update in database
                userRepo.updateSubscription(user.getId(), isActive, expiryDate,
                        subscriptionId, !willCancel);

                logger.info("Updated subscription for user " + user.getId() +
                        ", active: " + isActive + ", autoRenew: " + !willCancel);
            }

        } catch (Exception e) {
            logger.severe("Error processing subscription update: " + e.getMessage());
        }
    }

    private void handleSubscriptionCanceled(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObj = dataObjectDeserializer.getObject().orElseThrow();
        Subscription subscription = (Subscription) stripeObj;

        String subscriptionId = subscription.getId();

        try {
            // Find user with this subscription ID
            Optional<User> userOpt = userRepo.findBySubscriptionId(subscriptionId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Set premium to false and clear subscription data
                user.setPremiumStatus(false);
                user.setAutoRenew(false);

                // Update in database - keep the subscription ID for records
                userRepo.updateSubscription(user.getId(), false, user.getPremiumExpiry(),
                        subscriptionId, false);

                logger.info("Subscription canceled for user " + user.getId());
            }
        } catch (Exception e) {
            logger.severe("Error processing subscription cancellation: " + e.getMessage());
        }
    }

}
