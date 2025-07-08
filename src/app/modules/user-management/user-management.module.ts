import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import userManagementRoutes from './user-management.routes';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(userManagementRoutes)
    ],
    providers: [],
})
export class UserManagementModule { }