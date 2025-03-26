import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { SubscriptionService } from '../../services/subscription.service';
import { UserProfileService } from '../../services/user-profile.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserProfile } from '../../models/user-profile';
import { Subscription } from 'rxjs';
import { Location } from '@angular/common';

@Component({
  selector: 'app-premium-subscription',
  standalone: false,
  templateUrl: './premium-subscription.component.html',
  styleUrl: './premium-subscription.component.css'
})
export class PremiumSubscriptionComponent implements OnInit, OnDestroy {

  // Dependency injection
  private subService = inject(SubscriptionService);
  private profileService = inject(UserProfileService);
  private snackBar = inject(MatSnackBar);
  private location = inject(Location);

  // Component state
  protected loading = false;
  protected cancelling = false;
  protected userProfile: UserProfile | null = null;

  // Subscriptions
  private profileSub?: Subscription;
  private subscriptionSub?: Subscription;
  private reactivateSub?: Subscription;
  private cancelSub?: Subscription;

  ngOnInit(): void {
    this.loadUserProfile();
  }

  ngOnDestroy(): void {
    if (this.profileSub) this.profileSub.unsubscribe();
    if (this.subscriptionSub) this.subscriptionSub.unsubscribe();
    if (this.reactivateSub) this.reactivateSub.unsubscribe();
    if (this.cancelSub) this.cancelSub.unsubscribe();
  }

  private loadUserProfile(): void {
    this.loading = true;
    this.profileSub = this.profileService.getUserProfile().subscribe({
      next: (profile: UserProfile) => {
        this.userProfile = profile;
        this.loading = false;
      },
      error: (error) => {
        console.error("Error loading profile: ", error);
        this.snackBar.open("Failed to load profile. Please try again.", "Close", { duration: 5000 });
        this.loading = false;
      }
    });
  }

  subscribe(): void {
    this.loading = true;
    this.subscriptionSub = this.subService.createCheckoutSession().subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.url) {
          // Redirect to Stripe checkout
          window.location.href = response.url;
        } else {
          this.snackBar.open("Failed to create checkout session.", "Close", { duration: 5000 });
        }
      },
      error: (error) => {
        console.error('Error creating checkout session:', error);
        this.snackBar.open('Failed to create checkout session. Please try again.', 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  reactivateSubscription(): void {
    this.loading = true;
    this.reactivateSub = this.subService.reactivateSubscription().subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.snackBar.open('Your subscription has been reactivated and will continue after the current billing period.', 'Close', { duration: 5000 });
          // Refresh profile data to update UI
          this.loadUserProfile();
        } else {
          this.snackBar.open('Failed to reactivate subscription. Please try again.', 'Close', { duration: 5000 });
        }
      },
      error: (error) => {
        console.error('Error reactivating subscription:', error);
        this.snackBar.open('Failed to reactivate subscription. Please try again.', 'Close', { duration: 5000 });
        this.loading = false;
      }
    });
  }

  // Cancel subscription
  cancelSubscription(): void {
    if (confirm('Are you sure you want to cancel your premium subscription? Your benefits will continue until the end of your billing period.')) {
      this.cancelling = true;
      this.cancelSub = this.subService.cancelSubscription().subscribe({
        next: (response) => {
          this.cancelling = false;
          if (response.success) {
            this.snackBar.open('Your subscription has been canceled. You will retain premium benefits until the end of your billing period.', 'Close', { duration: 5000 });
            // Refresh profile data to update UI
            this.loadUserProfile();
          } else {
            this.snackBar.open('Failed to cancel subscription. Please try again.', 'Close', { duration: 5000 });
          }
        },
        error: (error) => {
          console.error('Error canceling subscription:', error);
          this.snackBar.open('Failed to cancel subscription. Please try again.', 'Close', { duration: 5000 });
          this.cancelling = false;
        }
      });
    }
  }

  // Go back to previous page
  goBack(): void {
    this.location.back();
  }

  // Format date for display
  formatDate(dateString: string | null | undefined): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  }

}
