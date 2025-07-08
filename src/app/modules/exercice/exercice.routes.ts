import { Routes } from '@angular/router';
import { ExerciceListComponent } from './exercice-list.component';
import { ExerciseUpsertComponent } from './exercice-upsert.component';

export default [
    {
        path: '',
        component: ExerciceListComponent
    },
    {
        path: 'create',
        component: ExerciseUpsertComponent
    },
    {
        path: 'edit/:id',
        component: ExerciseUpsertComponent
    }
] as Routes;