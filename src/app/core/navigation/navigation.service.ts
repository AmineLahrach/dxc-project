import { inject, Injectable } from '@angular/core';
import { Navigation } from 'app/core/navigation/navigation.types';
import { Observable, of, ReplaySubject, tap, take } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NavigationService {
    private _navigation: ReplaySubject<Navigation> =
        new ReplaySubject<Navigation>(1);

    // Mock navigation data
    private _mockNavigation: Navigation = {
        compact: [],
        default: [
            {id: 'dashboard', title: 'Dashboard', type: 'basic', icon: 'heroicons_outline:home', link: '/dashboard'},            
            {id: 'plans', title: 'Action Plans', subtitle: 'Manage strategic plans', type: 'collapsable', icon: 'heroicons_outline:document-text',
                children: [
                    {id: 'plans.list', title: 'All Plans', type: 'basic', icon: 'heroicons_outline:list-bullet', link: '/plans'},
                    {id: 'plans.create', title: 'Create Plan', type: 'basic', icon: 'heroicons_outline:plus', link: '/plans/create'}
                ]
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
                    { id: 'user', title: 'User Management', type: 'basic', icon: 'heroicons_outline:users',link: '/user' },
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
                id: 'admin.audit',
                title: 'Audit Logs',
                type: 'basic',
                icon: 'heroicons_outline:eye',
                link: '/admin/audit'
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
        futuristic: [],
        horizontal: []
    };

    get navigation$(): Observable<Navigation> {
        return this._navigation.asObservable();
    }

    get(): Observable<Navigation> {
        return this._navigation.asObservable().pipe(take(1));
    }

    getAll(): Observable<Navigation> {
        return of(this._mockNavigation).pipe(
            tap((navigation) => {
                this._navigation.next(navigation);
            })
        );
    }

    filterNavigationByRoles(navigation: Navigation, userRoles: string[]): Navigation {
        const isAllowed = (item: any) => {
            if (!item.meta || !item.meta.roles) return true;
            return item.meta.roles.some((role: string) => userRoles.includes(role));
        };
        const filterItems = (items: any[]) => items
            .filter(isAllowed)
            .map(item => ({
                ...item,
                children: item.children ? filterItems(item.children) : undefined
            }));

        return {
            compact: filterItems(navigation.compact || []),
            default: filterItems(navigation.default || []),
            futuristic: filterItems(navigation.futuristic || []),
            horizontal: filterItems(navigation.horizontal || [])
        };
    }
}
