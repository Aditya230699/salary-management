import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Attaches the bearer token and reacts to an expired or rejected one.
 *
 * Without the 401 branch a token that expired mid-session left every request failing
 * while the app still believed it was logged in, so the user saw empty screens with no
 * way to recover other than clearing storage by hand.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();
  const authorised = token
    ? req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) })
    : req;

  return next(authorised).pipe(
    catchError((error: HttpErrorResponse) => {
      // A failed login attempt legitimately returns 401 and is handled by the login
      // screen, so it must not trigger a redirect loop.
      const isLoginRequest = req.url.includes('/auth/login');

      if (error.status === 401 && !isLoginRequest) {
        authService.logout();
        router.navigate(['/login'], { queryParams: { reason: 'session-expired' } });
      }
      return throwError(() => error);
    })
  );
};
