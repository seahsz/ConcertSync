import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-email-verification',
  standalone: false,
  templateUrl: './email-verification.component.html',
  styleUrl: './email-verification.component.css'
})
export class EmailVerificationComponent implements OnInit, OnDestroy {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authSvc = inject(AuthService);
  private snackBar = inject(MatSnackBar);

  protected verificationState: "loading" | "success" | "error" = "loading";
  protected errorMessage = "";

  // Manage subscriptions
  private routeSubscription!: Subscription;
  private verifySubscription!: Subscription;

  ngOnInit(): void {
    this.routeSubscription = this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        this.verifyEmail(token);
      } else {
        this.verificationState = "error";
        this.errorMessage = "Invalid verification link. No token provided";
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routeSubscription)
        this.routeSubscription.unsubscribe();
    if (this.verifySubscription)
        this.verifySubscription.unsubscribe();
  }

  private verifyEmail(token: string): void {
    this.verifySubscription = this.authSvc.verifyEmail(token).subscribe({
      next: (response) => {
        this.verificationState = 'success';
        this.snackBar.open("Email verified successfully! You can login now", "Close",
          { duration: 5000, panelClass: "success-snackbar" });
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 5000);
      },
      error: (error) => {
        this.verificationState = "error";
        if (error.errors.invalid_token) {
          this.errorMessage = "The verification link is invalid or has expired";
        } else {
          this.errorMessage = "An error occurred during verification. Please try again later";
        }
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
