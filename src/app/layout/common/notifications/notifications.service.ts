import { Injectable } from '@angular/core';
import { Notification } from 'app/layout/common/notifications/notifications.types';
import { map, Observable, ReplaySubject, of, switchMap, take, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
    private _notifications: ReplaySubject<Notification[]> = new ReplaySubject<
        Notification[]
    >(1);

    // Static notifications data
    private _notificationsData: Notification[] = [
        {
            id: '493190c9-5b61-4912-afe5-78c21f1044d7',
            icon: 'heroicons_outline:check-circle',
            title: 'Daily challenges',
            description: 'Your submission has been accepted',
            time: '2023-05-25T13:00:00.000Z',
            read: false,
        },
        {
            id: '6e3e97e5-effc-4fb7-b730-52a151f0b641',
            image: 'images/avatars/male-04.jpg',
            description: 'Static notification example',
            time: '2023-05-26T15:30:00.000Z',
            read: true,
            link: '/dashboards/project',
            useRouter: true,
        },
        {
            id: '4cf56428-f9c4-44d4-9be2-a4b85e12b4a6',
            icon: 'heroicons_outline:exclamation',
            title: 'Attention required',
            description: 'Your project needs your attention',
            time: '2023-05-26T08:25:00.000Z',
            read: false,
        },
    ];

    /**
     * Constructor
     */
    constructor() {
        // Initialize with static data
        this._notifications.next(this._notificationsData);
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for notifications
     */
    get notifications$(): Observable<Notification[]> {
        return this._notifications.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get all notifications
     */
    getAll(): Observable<Notification[]> {
        // Return static data instead of HTTP call
        return of(this._notificationsData).pipe(
            tap((notifications) => {
                this._notifications.next(notifications);
            })
        );
    }

    /**
     * Create a notification
     *
     * @param notification
     */
    create(notification: Notification): Observable<Notification> {
        return this.notifications$.pipe(
            take(1),
            switchMap((notifications) => {
                // Create a new notification with a unique ID
                const newNotification = {
                    ...notification,
                    id: this._generateUniqueId(),
                };

                // Update local data
                this._notificationsData = [...this._notificationsData, newNotification];

                // Update the notifications
                this._notifications.next(this._notificationsData);

                // Return the new notification
                return of(newNotification);
            })
        );
    }

    /**
     * Update the notification
     *
     * @param id
     * @param notification
     */
    update(id: string, notification: Notification): Observable<Notification> {
        return this.notifications$.pipe(
            take(1),
            switchMap((notifications) => {
                // Find the index of the notification
                const index = this._notificationsData.findIndex(
                    (item) => item.id === id
                );

                if (index === -1) {
                    return of(null);
                }

                // Update the notification
                const updatedNotification = {
                    ...this._notificationsData[index],
                    ...notification,
                };

                this._notificationsData[index] = updatedNotification;

                // Update notifications
                this._notifications.next([...this._notificationsData]);

                // Return the updated notification
                return of(updatedNotification);
            })
        );
    }

    /**
     * Delete the notification
     *
     * @param id
     */
    delete(id: string): Observable<boolean> {
        return this.notifications$.pipe(
            take(1),
            switchMap((notifications) => {
                // Find the index of the deleted notification
                const index = this._notificationsData.findIndex(
                    (item) => item.id === id
                );

                if (index === -1) {
                    return of(false);
                }

                // Delete the notification
                this._notificationsData.splice(index, 1);

                // Update the notifications
                this._notifications.next([...this._notificationsData]);

                // Return the deleted status
                return of(true);
            })
        );
    }

    /**
     * Mark all notifications as read
     */
    markAllAsRead(): Observable<boolean> {
        return this.notifications$.pipe(
            take(1),
            map((notifications) => {
                // Go through all notifications and set them as read
                this._notificationsData = this._notificationsData.map((notification) => ({
                    ...notification,
                    read: true,
                }));

                // Update the notifications
                this._notifications.next([...this._notificationsData]);

                // Return the updated status
                return true;
            })
        );
    }

    /**
     * Generate a unique ID
     * Simple implementation - replace with a more robust one if needed
     */
    private _generateUniqueId(): string {
        return Date.now().toString(36) + Math.random().toString(36).substring(2);
    }
}
