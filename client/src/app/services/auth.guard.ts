import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "./auth.service";
import { MatSnackBar } from "@angular/material/snack-bar";

export const AuthGuard: CanActivateFn = (route, state) => {
    const authSvc = inject(AuthService);
    const router = inject(Router);
    const snackBar = inject(MatSnackBar);

    if (authSvc.isLoggedIn())
        return true;

    // User is not logged in, redirect to login page
    snackBar.open("Please log in to access this page", "Close", {
        duration: 3000,
        panelClass: "warning-snackbar"
    });

    // To facilitate going to the supposed destination after use log ins
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });

    return false;
}