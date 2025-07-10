import { Routes } from '@angular/router';
import { ServiceLineListComponent } from './service-line-list.component';
import { ServiceLineFormComponent } from './service-line-form.component';

export default [
  {
    path: '',
    component: ServiceLineListComponent,
    title: 'Service Lines'
  },
  {
    path: 'create',
    component: ServiceLineFormComponent,
    title: 'Create Service Line'
  },
  {
    path: 'edit/:id',
    component: ServiceLineFormComponent,
    title: 'Edit Service Line'
  }
] as Routes;