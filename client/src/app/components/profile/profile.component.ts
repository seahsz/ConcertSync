import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserProfileService } from '../../services/user-profile.service';
import { UserProfile } from '../../models/user-profile';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit, OnDestroy {

  private fb = inject(FormBuilder);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private authSvc = inject(AuthService);
  private profileSvc = inject(UserProfileService);

  // UI state
  protected userProfile: UserProfile | null = null;
  protected loading = true;
  protected error = false;
  protected activeTab = "profile"; // "profile", "concert", "settings"
  protected isEditingPhone = false;
  protected isEditingName = false;
  protected submitting = false;
  protected selectedFile: File | null = null;
  protected uploadingImage = false;

  // Forms
  protected phoneForm!: FormGroup;
  protected nameForm!: FormGroup;

  // Subscriptions
  private profileSub?: Subscription;
  private updatePhoneSub?: Subscription;
  private updateNameSub?: Subscription;
  private updateProfilePictureSub?: Subscription;

  ngOnInit(): void {
    this.initForms();
    this.loadUserProfile();
  }

  ngOnDestroy(): void {
    this.profileSub?.unsubscribe();
    this.updatePhoneSub?.unsubscribe();
    this.updateNameSub?.unsubscribe();
    this.updateProfilePictureSub?.unsubscribe();
  }

  private initForms(): void {
    this.phoneForm = this.fb.group({
      phoneNumber: ['', [Validators.pattern(/^\+65[89]\d{7}$/)]]
    });

    this.nameForm = this.fb.group({
      name: ["", [Validators.required]]
    });
  }


  loadUserProfile(): void {
    this.loading = true;
    this.profileSub = this.profileSvc.getUserProfile().subscribe({
      next: (response) => {
        this.userProfile = response;

        // Initialize forms with current value
        this.phoneForm.patchValue({
          phoneNumber: this.userProfile.phone_number || ""
        });

        this.nameForm.patchValue({
          name: this.userProfile.name || ""
        });

        this.loading = false;
      },
      error: (error) => {
        console.error("Error loading profile", error);
        this.snackBar.open("Failed to load profile. Please try again later.", "Close", { duration: 5000 });
        this.loading = false;
        this.error = true;
      }
    })
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  toggleEditPhone(): void {
    this.isEditingPhone = !this.isEditingPhone;

    if (!this.isEditingPhone && this.userProfile) {
      this.phoneForm.patchValue({
        phoneNumber: this.userProfile.phone_number || ""
      });
    }
  }

  toggleEditName(): void {
    this.isEditingName = !this.isEditingName;
    
    if (!this.isEditingName && this.userProfile) {
      this.nameForm.patchValue({
        name: this.userProfile.name
      });
    }
  }

  updatePhoneNumber(): void {
    if (this.phoneForm.valid && !this.submitting) {
      this.submitting = true;

      const phoneNumber = this.phoneForm.value.phoneNumber;

      this.updatePhoneSub = this.profileSvc.updatePhoneNumber(phoneNumber).subscribe({
        next: (response) => {
          this.userProfile = response;
          this.snackBar.open("Phone number updated successfully", "Close", { duration: 5000 });
          this.isEditingPhone = false;
          this.submitting = false;
        },
        error: (error) => {
          console.error("Error updating phone number", error);
          this.snackBar.open("Failed to update phone number. Please try again.", "Close", { duration: 5000 });
          this.submitting = false;
        }
      });
    }
  }

  updateName(): void {
    if (this.nameForm.valid && !this.submitting) {
      this.submitting = true;
      
      const name = this.nameForm.value.name;
      
      this.updateNameSub = this.profileSvc.updateName(name).subscribe({
        next: (response) => {
          this.userProfile = response;
          this.snackBar.open('Name updated successfully', 'Close', { duration: 3000 });
          this.isEditingName = false;
          this.submitting = false;
        },
        error: (error) => {
          console.error('Error updating name', error);
          this.snackBar.open('Failed to update name. Please try again.', 'Close', { duration: 5000 });
          this.submitting = false;
        }
      });
    }
  }

  onFileSelected($event: Event): void {
    const input = $event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];
    }
  }

  uploadProfilePicture(): void {
    if (this.selectedFile && !this.uploadingImage) {
      this.uploadingImage = true;
      
      this.updateProfilePictureSub = this.profileSvc.updateProfilePicture(this.selectedFile).subscribe({
        next: (response) => {
          this.userProfile = response;
          this.selectedFile = null;
          this.snackBar.open('Profile picture updated successfully', 'Close', { duration: 3000 });
          this.uploadingImage = false;
        },
        error: (error) => {
          console.error('Error updating profile picture', error);
          this.snackBar.open('Failed to update profile picture. Please try again.', 'Close', { duration: 5000 });
          this.uploadingImage = false;
        }
      });
    }
  }
  
  logout(): void {
    this.authSvc.logout();
    this.router.navigate(['/']);
    this.snackBar.open('You have been logged out successfully', 'Close', { duration: 3000 });
  }

}
