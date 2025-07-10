import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandlerFn,
    HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from 'app/core/auth/auth.service';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { Observable, catchError, throwError } from 'rxjs';

/**
 * Intercept
 *
 * @param req
 * @param next
 */
export const authInterceptor = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const authService = inject(AuthService);

    // Clone the request object
    let newReq = req.clone();

    // Request
    //
    // If the access token didn't expire, add the Authorization header.
    // We won't add the Authorization header if the access token expired.
    // This will force the server to return a "401 Unauthorized" response
    // for the protected API routes which our response interceptor will
    // catch and delete the access token from the local storage while logging
    // the user out from the app.
    if (
        authService.accessToken &&
        !AuthUtils.isTokenExpired(authService.accessToken)
    ) {
        newReq = req.clone({
            headers: req.headers.set(
                'Authorization',
                'Bearer ' + authService.accessToken
            ),
        });
    }

    // Response
    return next(newReq).pipe(
        catchError((error) => {
            // Catch "401 Unauthorized" responses
            if (error instanceof HttpErrorResponse && error.status === 401) {
                // Check if it's a real authentication failure or just an API error
                // We'll only log out if it's a specific authentication endpoint or has a specific error message
                const isAuthEndpoint = req.url.includes('/api/auth/');
                const hasAuthError = error.error && (
                    error.error.message === 'Invalid token' || 
                    error.error.message === 'Token expired' ||
                    error.error.message === 'Authentication failed'
                );
                
                if (isAuthEndpoint || hasAuthError) {
                    // It's a real authentication issue - sign out
                    console.log('Authentication failed. Signing out...');
                    authService.signOut();
                    
                    // Reload the app
                    location.reload();
                } else {
                    // It's just an API error, let the calling component handle it
                    console.warn('API returned 401 but not logging out: ', req.url);
                }
            }

            return throwError(() => error);
        })
    );
};
