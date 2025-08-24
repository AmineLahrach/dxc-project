import { Injectable } from '@angular/core';
import { Notification, Page } from 'app/layout/common/notifications/notifications.types';
import { map, Observable, ReplaySubject, of, switchMap, take, tap, BehaviorSubject, interval } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';


@Injectable({ providedIn: 'root' })
export class NotificationsService {

    private _notifications = new BehaviorSubject<Notification[]>([]);
    notifications$: Observable<Notification[]> = this._notifications.asObservable();

    private _unreadCount = new BehaviorSubject<number>(0);
    unreadCount$ = this._unreadCount.asObservable();

    private apiUrl = `${environment.apiUrl}/notification`;

    constructor(private http: HttpClient) { 
        this.startUnreadCountPolling();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for notifications
     */
    private get notifications(): Notification[] {
        return this._notifications.value;
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get all notifications
     */
    getAllUnread(): Observable<Notification[]> {
        return this.http.get<Notification[]>(this.apiUrl + '/all-unread');
    }

    private fetchUnreadCount() {
        return this.http.get<number>(`${this.apiUrl}/unread-count`);
    }

    private startUnreadCountPolling() {
        interval(30000) // 30 seconds
        .pipe(switchMap(() => this.fetchUnreadCount()))
        .subscribe(count => {
            this._unreadCount.next(count);
        });
    }

    getAll(page: number = 0, size: number = 10): Observable<Notification[]> {
        let params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        
        return this.http.get<Page<Notification>>(this.apiUrl + '/all', { params }).pipe(
            map((response: Page<Notification>) => response.content),
            tap((notifications: Notification[]) => {
                this._notifications.next(notifications);

                const unreadCount = notifications.filter(n => !n.recu).length;
                this._unreadCount.next(unreadCount);
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
                    oldId: this._generateUniqueId(),
                };

                // // Update local data
                // this._notificationsData = [...this._notificationsData, newNotification];

                // // Update the notifications
                // this._notifications.next(this._notificationsData);

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
                // const index = this._notificationsData.findIndex(
                //     (item) => item.id === id
                // );

                // if (index === -1) {
                //     return of(null);
                // }

                // // Update the notification
                // const updatedNotification = {
                //     ...this._notificationsData[index],
                //     ...notification,
                // };

                // this._notificationsData[index] = updatedNotification;

                // // Update notifications
                // this._notifications.next([...this._notificationsData]);

                // Return the updated notification
                return of(null);
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
                // const index = this._notificationsData.findIndex(
                //     (item) => item.id === id
                // );

                // if (index === -1) {
                //     return of(false);
                // }

                // // Delete the notification
                // this._notificationsData.splice(index, 1);

                // // Update the notifications
                // this._notifications.next([...this._notificationsData]);

                // Return the deleted status
                return of(true);
            })
        );
    }

    /**
     * Mark all notifications as read
     */
    markAllAsRead(): Observable<boolean> {
        return this.http.put<any>(this.apiUrl + '/read-all', {}).pipe(
            tap(() => {
                const updated = this.notifications.map(n =>
                    !n.recu ? { ...n, recu: true } : n
                );
                this._notifications.next(updated);
                const unreadCount = updated.filter(n => !n.recu).length;
                this._unreadCount.next(unreadCount);
            })
        )
    }

    /**
     * Mark notifications as read
     */
    markAsRead(notificationId: Number): Observable<boolean> {
        return this.http.put<any>(this.apiUrl + `/${notificationId}/read`, {}).pipe(
            tap(() => {
                const updated = this.notifications.map(n =>
                    n.id === notificationId ? { ...n, recu: true } : n
                );
                this._notifications.next(updated);
                const unreadCount = updated.filter(n => !n.recu).length;
                this._unreadCount.next(unreadCount);
            })
        );
    }

    /**
     * Mark notifications as unread
     */
    markAsUnread(notificationId: Number): Observable<boolean> {
        return this.http.put<any>(this.apiUrl + `/${notificationId}/unread`, {}).pipe(
            tap(() => {
                const updated = this.notifications.map(n =>
                    n.id === notificationId ? { ...n, recu: false } : n
                );
                this._notifications.next(updated);
                const unreadCount = updated.filter(n => !n.recu).length;
                this._unreadCount.next(unreadCount);
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
