import { Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { DirectorDashboardComponent } from './director-dashboard/director-dashboard.component';
import { CollaboratorDashboardComponent } from './collaborator-dashboard/collaborator-dashboard.component';

export default [
  {
    path: 'admin-dashboard',
    component: AdminDashboardComponent,
    title: 'Admin Dashboard'
  },
  {
    path: 'director-dashboard',
    component: DirectorDashboardComponent,
    title: 'Director Dashboard'
  },
  {
    path: 'collaborator-dashboard',
    component: CollaboratorDashboardComponent,
    title: 'Collaborator Dashboard'
  }
] as Routes;