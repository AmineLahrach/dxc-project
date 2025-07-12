import { inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, Router } from '@angular/router';
import { AuthService } from 'app/core/auth/auth.service';
import { map, of, switchMap } from 'rxjs';

export const NoAuthGuard: CanActivateFn | CanActivateChildFn = (
    route,
    state
) => {
    const router: Router = inject(Router);
    const authService: AuthService = inject(AuthService);

    // Check the authentication status
    return authService.check().pipe(
        switchMap((authenticated) => {
            // If the user is authenticated...
            if (authenticated) {
                // Determine which dashboard to redirect to based on user role
                if (authService.isAdmin()) {
                    return of(router.parseUrl('/dashboard/admin-dashboard'));
                } else if (authService.isDirector()) {
                    return of(router.parseUrl('/dashboard/director-dashboard'));
                } else if (authService.isCollaborator()) {
                    return of(router.parseUrl('/dashboard/collaborator-dashboard'));
                } else {
                    // Default dashboard if no specific role is matched
                    return of(router.parseUrl('/dashboard'));
                }
            }

            // Allow the access
            return of(true);
        })
    );
};
