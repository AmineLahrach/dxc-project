import { Routes } from '@angular/router';
import { PlanListComponent } from './plan-list/plan-list.component';
import { PlanDetailComponent } from './plan-detail/plan-detail.component';
import { PlanEditComponent } from './plan-edit/plan-edit.component';

export default [
    {
        path: '',
        component: PlanListComponent
    },
    {
        path: 'detail/:id',
        component: PlanDetailComponent
    },
    {
        path: 'edit/:id',
        component: PlanEditComponent
    },
    {
        path: 'create',
        component: PlanEditComponent
    }
] as Routes;