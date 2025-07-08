import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import planManagementRoutes from './plan-management.routes';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(planManagementRoutes)
    ],
    providers: [],
})
export class PlanManagementModule { }