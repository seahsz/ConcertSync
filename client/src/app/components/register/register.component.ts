import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { RegisterRequest } from '../../models/register-request';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit, OnDestroy {

  constructor () {
    console.info("Constructing RegisterComponent");
    const today = new Date();
    this.maxDate = new Date(today.getFullYear() - 16, today.getMonth(), today.getDay());
  }

  private fb = inject(FormBuilder);
  private authSvc = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  // Members
  protected registerForm !: FormGroup;
  protected submitting = false;
  protected maxDate: Date;
  protected hidePassword = true;
  protected hideConfirmPassword = true;
  protected registrationSuccess = false;

  // Error flags
  protected usernameTakenError = false;
  protected emailTakenError = false;

  // Manage subscriptions
  protected registerSub !: Subscription;
  protected passwordSub ?: Subscription;

  ngOnInit(): void {
    this.initForm();
    // Add the custom passwordMatch validator after form initialization to avoid circular reference
    this.registerForm.get('confirmPassword')?.setValidators([
      Validators.required,
      this.passwordMatchValidator.bind(this)
    ]);

    // Update confirmPassword validation when password changes
    this.passwordSub = this.registerForm.get("password")?.valueChanges.subscribe(() => {
      this.registerForm.get("confirmPassword")?.updateValueAndValidity();
    })
  }

  ngOnDestroy(): void {
    if (this.registerSub)
        this.registerSub.unsubscribe();
    if (this.passwordSub)
        this.passwordSub.unsubscribe();
  }

  private phoneNumberValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (value === "")
        return null; // Let the required validator handle empty values
    // Pattern: starts with +65, followed by 8 digits where first digit is 8 or 9
    const pattern = /^\+65[89]\d{7}$/;
    return pattern.test(value) ? null : { invalidPhoneNumber: true };
  }

  private passwordValidator(control: AbstractControl): ValidationErrors | null {
    const value: string = control.value;
    if (!value) {
      return null; // Let the required validator handle empty values
    }

    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumber = /[0-9]/.test(value);
    const hasMinLength = value.length >= 8; // Technically not needed? Since there is a Validators.minLength

    const passwordValid = hasUpperCase && hasLowerCase && hasNumber && hasMinLength;

    if (!passwordValid) {
      return {
        invalidPassword: { hasUpperCase, hasLowerCase, hasNumber, hasMinLength }
      };
    }
    return null;
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = this.registerForm.get("password")?.value;
    const confirmPassword = control.value;
    return password === confirmPassword ? null : { passwordMismatch : true };
  }

  private initForm(): void {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, this.passwordValidator]],
      confirmPassword: ['', Validators.required],
      name: ['', Validators.required],
      birthDate: ['', Validators.required],
      phoneNumber: ['', this.phoneNumberValidator]
    })
  }

  private handleErrors(errors: any): void {
    if (!errors) {
      this.snackBar.open("An unexpected error occurred. Please try again", "Close", {duration: 5000});
      return;
    }

    if (errors.username_taken) {
      this.usernameTakenError = true;
      this.registerForm.get("username")?.setErrors({ usernameTaken: true });
    }

    if (errors.email_taken) {
      this.emailTakenError = true;
      this.registerForm.get("email")?.setErrors({ emailTaken: true });
    }

    if (errors.unexpected) {
      this.snackBar.open("An unexpected error occurred. Please try again", "Close", {duration: 5000});
    }
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup)
          this.markFormGroupTouched(control);
    });
  }

  private showSuccessMessage(): void {
    this.registrationSuccess = true // set the success flag
    this.snackBar.open("Registration successful. Please verify your email before logging in.", "Close", {
      duration: 10000
    });
    // Reset form after successful registration
    this.registerForm.disable();

    // Redirect to login page after a delay
    setTimeout(() => {
      this.router.navigate(['/login']);
    }, 5000);
  }

  onSubmit(): void {
    if (this.registerForm.valid && !this.submitting) {
      this.submitting = true;
      // reset error flags
      this.usernameTakenError = false;
      this.emailTakenError = false;

      // Prepare the payload
      const formValue = this.registerForm.value;
      const registerData: RegisterRequest = {
        username: formValue.username,
        email: formValue.email,
        password: formValue.password,
        name: formValue.name,
        birthDate: formValue.birthDate,
        phoneNumber: formValue.phoneNumber
      }

      // Subscripe to the response
      this.registerSub = this.authSvc.register(registerData).subscribe({
        next: (response) => {
          if (response.success) {
            this.showSuccessMessage();
          } else {
            this.handleErrors(response.errors);
          }
          this.submitting = false;
        },
        error: (error) => {
          this.handleErrors(error.errors);
          this.submitting = false;
        }
      })
    } else {
      // Mark all fields as touched to show validation errors
      this.markFormGroupTouched(this.registerForm);
    }
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  getErrorMessage(controlName: string): string {

    // Just as a precuation: if registration was successful, don't show any errors
    if (this.registrationSuccess)
      return "";

    // Handle other controls
    const control = this.registerForm.get(controlName);

    if (!control || !control.errors || !control.touched)
      return "";

    if (control.errors["required"])
      return "This field is required";

    if (controlName === "password") {
      if (control.errors["invalidPassword"]) {
        const errors = control.errors["invalidPassword"];

        if (!errors.hasMinLength)
            return "Password must be at least 8 characters long";
        if (!errors.hasLowerCase)
            return "Password must contain at least one lowercase letter";
        if (!errors.hasUpperCase)
            return "Password must contain at least one uppercase letter";
        if (!errors.hasNumber)
            return "Password must contain at least one number";
      }
    }

    if (controlName === "confirmPassword") {
      if (control.errors["passwordMismatch"])
          return "Passwords do not match";
    }

    if (controlName === "birthDate") {
      if (control.errors['matDatepickerParse']) // NOT WORKING: TODO - FIX
        return "Invalid date format";
      if (control.errors['matDatepickerMax'])
        return "You must be at least 16 years old to register";
      return "Please enter a valid date";
    }

    // For username-related errors
    if (controlName === "username") {
      if (control.errors["minlength"])
          return "Username must be at least 3 characters";
      if (control.errors["maxlength"])
          return "Username cannot exceed 20 characters";
      if (control.errors["usernameTaken"])
          return "This username is already taken";
    }    

    // For email-related errors
    if (controlName === "email") {
      if (control.errors["email"])
          return "Please enter a valid email address";
      if (control.errors["emailTaken"])
          return "This email is already taken";
    }

    // For phoneNumber-related errors
    if (controlName === "phoneNumber" && control.errors["invalidPhoneNumber"])
        return "Phone number must start with +65 followed by 8 digits (first digit must be 8 or 9)";

    return "Invalid input"; // DEFAULT RETURN
  }


}
