import { Routes } from '@angular/router';
import { VariableListComponent } from './list/variable-list.component';
import { VariableUpsertComponent } from './upsert/variable-upsert.component';

export default [
  { path: '', component: VariableListComponent },
  { path: 'create', component: VariableUpsertComponent },
  { path: 'edit/:id', component: VariableUpsertComponent }
] as Routes;