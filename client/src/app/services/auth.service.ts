import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { RegisterRequest } from '../models/register-request';
import { catchError, map, Observable, throwError } from 'rxjs';
import { LoginRequest } from '../models/login-request';
import { AuthResponse } from '../models/auth-response';

import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private BASE_URL = '/api/auth';

  private http = inject(HttpClient);

  register(registerData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE_URL}/register`, registerData).pipe(
      catchError(error => {
        if (error.error) {
          return throwError(() => error.error as AuthResponse);
        }
        return throwError(() => ({
          success: false,
          error: { unexpected: true }
        } as AuthResponse));
      })
    );
  }

  verifyEmail(token: string): Observable<AuthResponse> {
    return this.http.get<AuthResponse>(`${this.BASE_URL}/verify-email?token=${token}`).pipe(
      catchError(error => {
        if (error.error) {
          return throwError(() => error.error as AuthResponse);
        }
        return throwError(() => ({
          success: false,
          error: { unexpected: true }
        } as AuthResponse ))
      })
    );
  }

  resendVerificationEmail(username: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE_URL}/resend-verification`, username).pipe(
      catchError(error => {
        if (error.error) {
          return throwError(() => error.error as AuthResponse);
        }
        return throwError(() => ({
          success: false,
          error: { unexpected: true }
        } as AuthResponse ))
      })
    );
  }

  login(loginData: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.BASE_URL}/login`, loginData).pipe(
      catchError(error => {
        if (error.error) {
          return throwError(() => error.error as AuthResponse);
        }
        return throwError(() => ({
          success: false,
          error: { unexpected: true }
        } as AuthResponse ))
      })
    );
  }

  saveToken(token: string): void {
    localStorage.setItem('auth_token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  removeToken(): void {
    localStorage.removeItem('auth_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    this.removeToken();
  }

  // Get current user ID from the JWT token
  getCurrentUserId(): number | null {
    try {
      const token = this.getToken();
      if (!token) return null;

      const decodedToken: any = jwtDecode(token);
      // Most JWT tokens store the user ID in the 'sub' claim
      const userId: number = parseInt(decodedToken.sub, 10);
      console.info("Getting userId: ", userId);
      return userId;
    } catch (error) {
      console.error("Error decoding token: ", error);
      return null;
    }
  }
}
