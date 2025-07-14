import { inject, Injectable } from '@angular/core';
import { FuseNavigationItem } from '@fuse/components/navigation';
import { Navigation } from 'app/core/navigation/navigation.types';
import { Observable, of, ReplaySubject, tap, take } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NavigationService {
    private _navigation: ReplaySubject<Navigation> =
        new ReplaySubject<Navigation>(1);

    // Define the default navigation once
    private _defaultNavItems : FuseNavigationItem[] = [
        {id: 'dashboard', title: 'Dashboard', type: 'basic', icon: 'heroicons_outline:home', link: '/dashboard/redirect-to-dashboard'},            
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
    ];

    // Mock navigation data - using the same items for all navigation types
    private _mockNavigation: Navigation = {
        default: [...this._defaultNavItems],
        compact: [...this._defaultNavItems],
        futuristic: [...this._defaultNavItems],
        horizontal: [...this._defaultNavItems]
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

    getAllByUserRoles(userRoles: string[]): Observable<Navigation> {
        const filteredNavigation = this.filterNavigationByRoles(this._mockNavigation, userRoles);
        return of(filteredNavigation).pipe(
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
        
        const filterItems = (items: any[]) => {
            // First filter based on roles
            const filteredItems = items.filter(isAllowed).map(item => ({
                ...item,
                children: item.children ? filterItems(item.children) : undefined
            }));
            
            // Then clean up consecutive dividers
            return cleanupDividers(filteredItems);
        };
        
        // Helper to clean up dividers
        const cleanupDividers = (items: any[]) => {
            const result: any[] = [];
            let lastWasDivider = false;
            
            for (const item of items) {
                // Skip if this is a divider and the last item was also a divider
                if (item.type === 'divider' && lastWasDivider) {
                    continue;
                }
                
                // Add this item to the result
                result.push(item);
                
                // Update the lastWasDivider flag
                lastWasDivider = item.type === 'divider';
            }
            
            // Remove leading or trailing dividers
            if (result.length > 0) {
                if (result[0].type === 'divider') {
                    result.shift();
                }
                if (result.length > 0 && result[result.length - 1].type === 'divider') {
                    result.pop();
                }
            }
            
            return result;
        };

        return {
            compact: filterItems(navigation.compact || []),
            default: filterItems(navigation.default || []),
            futuristic: filterItems(navigation.futuristic || []),
            horizontal: filterItems(navigation.horizontal || [])
        };
    }
}
