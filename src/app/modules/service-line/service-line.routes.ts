import { Routes } from '@angular/router';
import { ServiceLineListComponent } from './service-line-list.component';

export default [
  {
    path: '',
    component: ServiceLineListComponent,
    title: 'Service Lines'
  },
  {
    path: 'create',
    component: ServiceLineListComponent,
    title: 'Create Service Line'
  },
  {
    path: 'edit/:id',
    component: ServiceLineListComponent,
    title: 'Edit Service Line'
  }
] as Routes;