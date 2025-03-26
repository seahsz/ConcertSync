import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
import { LoginRequest } from '../../models/login-request';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit, OnDestroy {

  private fb = inject(FormBuilder);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);
  private authSvc = inject(AuthService);

  // Members
  protected loginForm!: FormGroup;
  protected submitting = false;
  protected hidePassword = true;
  protected resendingEmail = false;

  // Error flags
  protected invalidCredentials = false;
  protected emailUnverified = false;

  // Subscription management
  private loginSub?: Subscription;
  private resendVerificationSub?: Subscription;

  ngOnInit(): void {
    // Initialize form
    this.loginForm = this.fb.group({
      username: ["", Validators.required],
      password: ["", Validators.required],
      // rememberMe: [false]
    });

    // Redirect if user is already logged in
    if (this.authSvc.isLoggedIn()) {
      this.router.navigate(['/']); 
    }
  }

  ngOnDestroy(): void {
    if (this.loginSub)
        this.loginSub.unsubscribe();
    if (this.resendVerificationSub)
        this.resendVerificationSub.unsubscribe();
  }

  onSubmit(): void {
    if (this.loginForm.valid && !this.submitting) {
      this.submitting = true;
      this.invalidCredentials = false;
      this.emailUnverified = false;

      const loginData: LoginRequest = {
        username: this.loginForm.value.username,
        password: this.loginForm.value.password
      };

      this.loginSub = this.authSvc.login(loginData).subscribe({
        next: (response) => {
          if (response.success && response.token) {
              // Save the JWT Token
              this.authSvc.saveToken(response.token);

              // Check for returnUrl in the queryParam
              const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

              this.snackBar.open("Login successful!", "Close", { duration: 3000 });
              this.router.navigate([returnUrl]);
          } else {
            this.handleErrors(response.errors);
          }

          this.submitting = false;
        },
        error: (error) => {
          this.handleErrors(error.errors);
          this.submitting = false;
        }
      });
    }
  }

  private handleErrors(errors: any): void {
    // In case 1) Server returns empty response, 2) Network error, 3) Error is in an unexpected format
    if (!errors) {
      this.snackBar.open("An unexpected error occurred. Please try again.", "Close", { duration: 5000 });
      return;
    }

    if (errors.invalid_credentials)
        this.invalidCredentials = true;
    
    if (errors.email_unverified)
        this.emailUnverified = true;

    if (errors.unexpected)
        this.snackBar.open("An unexpected error occurred. Please try again.", "Close", { duration: 5000 });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  resendVerificationEmail(): void {
    if (!this.loginForm.value.username || this.resendingEmail) {
      return;
    }

    this.resendingEmail = true;
    
    this.resendVerificationSub = this.authSvc.resendVerificationEmail(this.loginForm.value.username).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.open("Verification email sent. Please check your inbox.", "Close", { duration: 5000 });
        } else {
          this.snackBar.open("Failed to send verification email. Please try again.", "Close", { duration: 5000 });
        }
        this.resendingEmail = false;
      },
      error: (error) => {
        if (error.errors?.user_not_found) {
          this.snackBar.open("User not found. Please check your username.", "Close", { duration: 5000 });
        } else {
          this.snackBar.open("Failed to send verification email. Please try again.", "Close", { duration: 5000 });
        }
        this.resendingEmail = false;
      }
    });
  }

}
