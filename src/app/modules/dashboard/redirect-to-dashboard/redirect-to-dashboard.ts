import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'app/core/auth/auth.service';

@Component({
  selector: 'app-redirect-to-dashboard',
  template: '<div>Redirecting...</div>'
})
export class RedirectToDashboardComponent implements OnInit {
  
  constructor(
    private _authService: AuthService,
    private _router: Router
  ) {}

  ngOnInit() {
    if (this._authService.isAdmin()) {
      this._router.navigate(['/dashboard/admin-dashboard']);
    } else if (this._authService.isDirector()) {
      this._router.navigate(['/dashboard/director-dashboard']);
    } else if (this._authService.isCollaborator()) {
      this._router.navigate(['/dashboard/collaborator-dashboard']);
    } else {
      // Default dashboard
      this._router.navigate(['/dashboard']);
    }
  }
}