import { Injectable } from '@angular/core';
import { Shortcut } from 'app/layout/common/shortcuts/shortcuts.types';
import { map, Observable, of, ReplaySubject, switchMap, take, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ShortcutsService {
    private _shortcuts: ReplaySubject<Shortcut[]> = new ReplaySubject<Shortcut[]>(1);

    // Static shortcuts data
    private _shortcutsData: Shortcut[] = [
        {
            id: '1',
            label: 'Calendar',
            description: 'Calendar application',
            icon: 'heroicons_outline:calendar',
            link: '/apps/calendar',
            useRouter: true
        },
        {
            id: '2',
            label: 'Contacts',
            description: 'Contact management',
            icon: 'heroicons_outline:user-group',
            link: '/apps/contacts',
            useRouter: true
        },
        {
            id: '3',
            label: 'Tasks',
            description: 'Task management application',
            icon: 'heroicons_outline:check-circle',
            link: '/apps/tasks',
            useRouter: true
        },
        {
            id: '4',
            label: 'Dashboard',
            description: 'Project dashboard',
            icon: 'heroicons_outline:chart-pie',
            link: '/dashboards/project',
            useRouter: true
        }
    ];

    /**
     * Constructor
     */
    constructor() {
        // Initialize with static data
        this._shortcuts.next(this._shortcutsData);
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for shortcuts
     */
    get shortcuts$(): Observable<Shortcut[]> {
        return this._shortcuts.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get all shortcuts
     */
    getAll(): Observable<Shortcut[]> {
        // Return static data instead of HTTP call
        return of(this._shortcutsData).pipe(
            tap((shortcuts) => {
                this._shortcuts.next(shortcuts);
            })
        );
    }

    /**
     * Create a shortcut
     *
     * @param shortcut
     */
    create(shortcut: Shortcut): Observable<Shortcut> {
        return this.shortcuts$.pipe(
            take(1),
            switchMap((shortcuts) => {
                // Generate a new ID
                const newShortcut = {
                    ...shortcut,
                    id: this._generateUniqueId()
                };

                // Update the shortcuts with the new shortcut
                this._shortcutsData = [...this._shortcutsData, newShortcut];
                this._shortcuts.next(this._shortcutsData);

                // Return the new shortcut
                return of(newShortcut);
            })
        );
    }

    /**
     * Update the shortcut
     *
     * @param id
     * @param shortcut
     */
    update(id: string, shortcut: Shortcut): Observable<Shortcut> {
        return this.shortcuts$.pipe(
            take(1),
            switchMap((shortcuts) => {
                // Find the index of the updated shortcut
                const index = this._shortcutsData.findIndex(item => item.id === id);

                if (index === -1) {
                    return of(null);
                }

                // Update the shortcut
                const updatedShortcut = {
                    ...this._shortcutsData[index],
                    ...shortcut
                };

                this._shortcutsData[index] = updatedShortcut;

                // Update the shortcuts
                this._shortcuts.next([...this._shortcutsData]);

                // Return the updated shortcut
                return of(updatedShortcut);
            })
        );
    }

    /**
     * Delete the shortcut
     *
     * @param id
     */
    delete(id: string): Observable<boolean> {
        return this.shortcuts$.pipe(
            take(1),
            switchMap((shortcuts) => {
                // Find the index of the deleted shortcut
                const index = this._shortcutsData.findIndex(item => item.id === id);

                if (index === -1) {
                    return of(false);
                }

                // Delete the shortcut
                this._shortcutsData.splice(index, 1);

                // Update the shortcuts
                this._shortcuts.next([...this._shortcutsData]);

                // Return the deleted status
                return of(true);
            })
        );
    }

    /**
     * Generate a unique ID
     */
    private _generateUniqueId(): string {
        return Date.now().toString(36) + Math.random().toString(36).substring(2);
    }
}
