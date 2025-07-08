import { Routes } from '@angular/router';
import { ProfileListComponent } from './profile-list.component';

export default [
  {
    path: '',
    component: ProfileListComponent,
    title: 'Profiles'
  },
  {
    path: 'create',
    component: ProfileListComponent,
    title: 'Create Profile'
  },
  {
    path: 'edit/:id',
    component: ProfileListComponent,
    title: 'Edit Profile'
  }
] as Routes;