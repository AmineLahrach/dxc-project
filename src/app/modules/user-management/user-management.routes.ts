import { Routes } from '@angular/router';
import { UserListComponent } from './user-list/user-list.component';
import { UserDetailComponent } from './user-detail/user-detail.component';
import { UserEditComponent } from './user-edit/user-edit.component';

export default [
    {
        path: '',
        component: UserListComponent
    },
    {
        path: 'detail/:id',
        component: UserDetailComponent
    },
    {
        path: 'edit/:id',
        component: UserEditComponent
    },
    {
        path: 'create',
        component: UserEditComponent
    }
] as Routes;