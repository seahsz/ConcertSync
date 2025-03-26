import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { UserProfile } from '../../../models/user-profile';
import { UserProfileService } from '../../../services/user-profile.service';

@Component({
  selector: 'app-shared-navbar',
  standalone: false,
  templateUrl: './shared-navbar.component.html',
  styleUrl: './shared-navbar.component.css'
})
export class SharedNavbarComponent implements OnInit, OnDestroy {

  private router = inject(Router);
  private authSvc = inject(AuthService);
  private profileSvc = inject(UserProfileService);

  protected isLoggedIn = false;
  protected userProfile: any = null;

  // Subscription
  private profileUpdateSub?: Subscription;

  ngOnInit(): void {
    this.isLoggedIn = this.authSvc.isLoggedIn();

    if (this.isLoggedIn)
        this.loadUserProfile();

    this.profileUpdateSub = this.profileSvc.profileUpdated.subscribe(
      (updatedProfile: UserProfile) => {
        console.log("[Shared Navbar] Navbar received profile update");
        this.updateNavbarProfile(updatedProfile);
      }
    )
  }

  ngOnDestroy(): void {
    this.profileUpdateSub?.unsubscribe();
  }

  loadUserProfile(): void {
    this.profileSvc.getUserProfile().subscribe({
      next: (profile) => {
        this.updateNavbarProfile(profile);
      },
      error: (error) => {
        console.error("[Shared-Navbar] Error loading profile in navbar:", error);
        // Use placeholder for errors
        this.userProfile = {
          username: "User",
          name: "Unknown User",
          email: "",
          profile_picture_url: "/images/blank_profile_pic_160px.png"
        };
      }
    })
  }

  updateNavbarProfile(profile: UserProfile): void {
    this.userProfile = {
      username: profile.username,
      name: profile.name,
      email: profile.email,
      profilePictureUrl: profile.profile_picture_url,
      premiumStatus: profile.premium_status
    };
  }

  navigateTo(route: string): void {
    // Check if we're already on this route
    if (this.router.url === route) {
      // Force a reload of the current route by navigating to it again with skipLocationChange
      this.router.navigateByUrl('/', {skipLocationChange: true}).then(() => {
        this.router.navigate([route]);
      });
    } else {
      // Normal navigation to a different route
      this.router.navigate([route]);
    }
  }

  logout(): void {
    this.authSvc.logout();
    this.isLoggedIn = false;
    this.userProfile = null;
    this.router.navigate(['/']);
  }

}
