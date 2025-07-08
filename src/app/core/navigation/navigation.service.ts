import { inject, Injectable } from '@angular/core';
import { Navigation } from 'app/core/navigation/navigation.types';
import { Observable, of, ReplaySubject, tap, take } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NavigationService {
    private _navigation: ReplaySubject<Navigation> =
        new ReplaySubject<Navigation>(1);

    // Mock navigation data
    private _mockNavigation: Navigation = {
        compact: [
            {
                id: 'dashboard',
                title: 'Dashboard',
                type: 'basic',
                icon: 'heroicons_outline:home',
                link: '/dashboard'
            },
            { id: 'about', title: 'About', type: 'basic', link: '/about' }
        ],
        default: [
            {
                id: 'dashboard', title: 'Dashboard', type: 'basic',
                icon: 'heroicons_outline:home', link: '/dashboard'
            },
            { id: 'user', title: 'User Management', type: 'basic', icon: 'heroicons_outline:users',link: '/user' },
            // { id: 'plan', title: 'Plan', type: 'basic', icon: 'heroicons_outline:plus',link: '/plans' },
            {
        id: 'plans',
        title: 'Action Plans',
        subtitle: 'Manage strategic plans',
        type: 'collapsable',
        icon: 'heroicons_outline:document-text',
        children: [
            {
                id: 'plans.list',
                title: 'All Plans',
                type: 'basic',
                icon: 'heroicons_outline:list-bullet',
                link: '/plans'
            },
            {
                id: 'plans.create',
                title: 'Create Plan',
                type: 'basic',
                icon: 'heroicons_outline:plus',
                link: '/plans/create'
            }
        ]
    },
    {
        id: 'admin.profiles',
        title: 'Profile Management',
        type: 'basic',
        icon: 'heroicons_outline:identification',
        link: '/profiles'
    },
    {
        id: 'admin.servicelines',
        title: 'Service Lines',
        type: 'basic',
        icon: 'heroicons_outline:building-office',
        link: '/service-lines'
    },
    {
        id: 'variables',
        title: 'Action Variables',
        subtitle: 'Manage plan variables',
        type: 'basic',
        icon: 'heroicons_outline:variable',
        link: '/variables'
    },
    {
        id: 'exercises',
        title: 'Exercises',
        subtitle: 'Yearly exercises',
        type: 'basic',
        icon: 'heroicons_outline:calendar',
        link: '/exercises',
        meta: {
            roles: ['ADMINISTRATEUR', 'DIRECTEUR_GENERAL']
        }
    },
    {
        id: 'divider-1',
        type: 'divider'
    },
    {
        id: 'admin',
        title: 'Administration',
        subtitle: 'System management',
        type: 'group',
        icon: 'heroicons_outline:cog-6-tooth',
        meta: {
            roles: ['ADMINISTRATEUR']
        },
        children: [
            {
                id: 'admin.users',
                title: 'User Management',
                type: 'basic',
                icon: 'heroicons_outline:users',
                link: '/admin/users'
            },            
            {
                id: 'admin.audit',
                title: 'Audit Logs',
                type: 'basic',
                icon: 'heroicons_outline:eye',
                link: '/admin/audit'
            }
        ]
    },
    {
        id: 'divider-2',
        type: 'divider'
    },
    {
        id: 'notifications',
        title: 'Notifications',
        subtitle: 'System alerts',
        type: 'basic',
        icon: 'heroicons_outline:bell',
        link: '/notifications',
        badge: {
            title: '3',
            classes: 'px-2 bg-pink-600 text-white rounded-full'
        }
    },
    {
        id: 'profile',
        title: 'Profile',
        subtitle: 'Account settings',
        type: 'basic',
        icon: 'heroicons_outline:user-circle',
        link: '/profile'
    }
        ],
        futuristic: [{ id: 'home', title: 'Homeb', type: 'basic', link: '/home' },
            { id: 'about', title: 'About', type: 'basic', link: '/about' }],
        horizontal: [
            { id: 'home', title: 'Homev', type: 'basic', link: '/home' },
            { id: 'about', title: 'About', type: 'basic', link: '/about' }
        ]
    };

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for navigation
     */
    get navigation$(): Observable<Navigation> {
        return this._navigation.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get all navigation data
     */
    // get(): Observable<Navigation> {
    //     // Emit mock data instead of making an HTTP request
    //     this._navigation.next(this._mockNavigation);
    //     return this._navigation.asObservable();
    // }

    get(): Observable<Navigation> {
        return this._navigation.asObservable().pipe(take(1));
    }

    getAll(): Observable<Navigation> {
        // Return static data instead of HTTP call
        return of(this._mockNavigation).pipe(
            tap((navigation) => {
                this._navigation.next(navigation);
            })
        );
    }
}
