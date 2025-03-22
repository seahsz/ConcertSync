import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Inject the AuthService
  const authService = inject(AuthService);

  // Get the token from the AuthService
  const token = authService.getToken();

  // Clone the request and add the Authorization header if token exists
  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    // Log for debugging
    // console.log('Interceptor adding token to request:', clonedRequest.headers.get('Authorization'));

    return next(clonedRequest);
  }

  // If no token, pass the original request
  return next(req);
};