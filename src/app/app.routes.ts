import { Route } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { AuthGuard } from 'app/core/auth/guards/auth.guard';
import { NoAuthGuard } from 'app/core/auth/guards/noAuth.guard';
import { LayoutComponent } from 'app/layout/layout.component';
import { RoleGuard } from './core/auth/guards/role.guard';
import { RedirectToDashboardComponent } from './modules/dashboard/redirect-to-dashboard/redirect-to-dashboard';
import { VariableListComponent } from './modules/variables/list/variable-list.component';

export const appRoutes: Route[] = [
    {path: '', pathMatch : 'full', redirectTo: 'sign-in'},
    {path: 'signed-in-redirect', 
        canActivate: [AuthGuard],
        component: RedirectToDashboardComponent
    },
    // Auth routes for guests
    {path: '', 
        canActivate: [NoAuthGuard],
        canActivateChild: [NoAuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            {path: 'confirmation-required', loadChildren: () => import('app/modules/auth/confirmation-required/confirmation-required.routes')},
            {path: 'forgot-password', loadChildren: () => import('app/modules/auth/forgot-password/forgot-password.routes')},
            {path: 'reset-password', loadChildren: () => import('app/modules/auth/reset-password/reset-password.routes')},
            {path: 'sign-in', loadChildren: () => import('app/modules/auth/sign-in/sign-in.routes')},
            {path: 'sign-up', loadChildren: () => import('app/modules/auth/sign-up/sign-up.routes')}
        ]
    },

    // Auth routes for authenticated users
    {
        path: '',
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        data: {
            layout: 'empty'
        },
        children: [
            {path: 'sign-out', loadChildren: () => import('app/modules/auth/sign-out/sign-out.routes')},
            {path: 'unlock-session', loadChildren: () => import('app/modules/auth/unlock-session/unlock-session.routes')}
        ]
    },
    {
        path: '',
        // canActivate: [AuthGuard],
        // canActivateChild: [AuthGuard],
        component: LayoutComponent,
        resolve: {
            initialData: initialDataResolver
        },
        children: [
            {
                path: 'dashboard',
                loadChildren: () => import('./modules/dashboard/dashboard.routes'),
                // canActivate: [AuthGuard, RoleGuard],
                // data: { roles: ['COLLABORATOR'] }
            },
            {path: 'example', loadChildren: () => import('app/modules/admin/example/example.routes')},
            {path: 'user', loadChildren: () => import('app/modules/user-management/user-management.routes')},
            {path: 'plans', loadChildren: () => import('app/modules/plan-management/plan-management.routes')},
            {path: 'profiles', loadChildren: () => import('app/modules/profile-form/profile.routes')},
            {path: 'service-lines', loadChildren: () => import('app/modules/service-line/service-line.routes')},
            {path: 'variables', component: VariableListComponent}
        ]
    },
    {
        path: '',
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        component: LayoutComponent,
        resolve: {
            initialData: initialDataResolver
        },
        children: [
            {path: 'exercises', loadChildren: () => import('app/modules/exercice/exercice.routes')},
        ]
    }
];
