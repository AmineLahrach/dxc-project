import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../auth.service';
import { Observable, of, map } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private _authService: AuthService,
    private _router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    const requiredRoles = route.data['roles'] as string[];
    if (!requiredRoles || requiredRoles.length === 0) {
      of(true);
    }

    return this._authService.currentUser$.pipe(
      map(user => {
        if (!user) {
          this._router.navigate(['/sign-out']);
          return false;
        }

        const hasRole = requiredRoles.some(role => user.roles?.includes(role));
        if (!hasRole) {
          this._router.navigate(['/sign-out']);
          return false;
        }

        return true;
      })
    );
  }
}