import { Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { DirectorDashboardComponent } from './director-dashboard/director-dashboard.component';
import { CollaboratorDashboardComponent } from './collaborator-dashboard/collaborator-dashboard.component';
import { AuthGuard } from 'app/core/auth/guards/auth.guard';
import { RoleGuard } from 'app/core/auth/guards/role.guard';
import { RedirectToDashboardComponent } from './redirect-to-dashboard/redirect-to-dashboard';

export default [
  {
    path: 'redirect-to-dashboard',
    component: RedirectToDashboardComponent,  
  },
  {
    path: 'admin-dashboard',
    component: AdminDashboardComponent,
    // canActivate: [AuthGuard, RoleGuard],
    // data: { roles: ['ROLE_ADMIN'] },
    title: 'Admin Dashboard'
  },
  {
    path: 'director-dashboard',
    component: DirectorDashboardComponent,
    // canActivate: [AuthGuard, RoleGuard],
    // data: { roles: ['ROLE_DIRECTOR'] },
    title: 'Director Dashboard'
  },
  {
    path: 'collaborator-dashboard',
    component: CollaboratorDashboardComponent,
    // canActivate: [AuthGuard],
    title: 'Collaborator Dashboard'
  }
] as Routes;