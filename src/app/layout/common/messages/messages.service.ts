import { Injectable } from '@angular/core';
import { Message } from 'app/layout/common/messages/messages.types';
import { map, Observable, ReplaySubject, of, switchMap, take, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class MessagesService {
    private _messages: ReplaySubject<Message[]> = new ReplaySubject<Message[]>(1);

    // Static messages data - replace with your own structure as needed
    private _messagesData: Message[] = [
        {
            id: '832c175d-42d2-47a0-9434-659e5354eb08',
            image: 'images/avatars/female-01.jpg',
            title: 'Welcome to the app',
            description: 'This is a static message',
            time: '2023-05-01T09:20:13.462Z',
            read: false,
            link: '/dashboards/project',
            useRouter: true
        },
        {
            id: '7d6dd47e-7b72-4427-9f70-4f0481cda581',
            image: 'images/avatars/male-01.jpg',
            title: 'Meeting reminder',
            description: 'This is another static message',
            time: '2023-05-02T11:30:00.462Z',
            read: true,
            link: '/apps/calendar',
            useRouter: true
        }
    ];

    /**
     * Constructor
     */
    constructor() {
        // Initialize with static data
        this._messages.next(this._messagesData);
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for messages
     */
    get messages$(): Observable<Message[]> {
        return this._messages.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get all messages
     */
    getAll(): Observable<Message[]> {
        // Return static data instead of HTTP call
        return of(this._messagesData).pipe(
            tap((messages) => {
                this._messages.next(messages);
            })
        );
    }

    /**
     * Create a message
     *
     * @param message
     */
    create(message: Message): Observable<Message> {
        return this.messages$.pipe(
            take(1),
            switchMap((messages) => {
                // Create a new message with a unique ID
                const newMessage = {
                    ...message,
                    id: this._generateUniqueId()
                };
                
                // Update local data
                this._messagesData = [...this._messagesData, newMessage];
                
                // Update the messages
                this._messages.next(this._messagesData);
                
                // Return the new message
                return of(newMessage);
            })
        );
    }

    /**
     * Update the message
     *
     * @param id
     * @param message
     */
    update(id: string, message: Message): Observable<Message> {
        return this.messages$.pipe(
            take(1),
            switchMap((messages) => {
                // Find message index
                const index = this._messagesData.findIndex(item => item.id === id);
                
                if (index === -1) {
                    return of(null);
                }
                
                // Update the message
                const updatedMessage = {
                    ...this._messagesData[index],
                    ...message
                };
                
                this._messagesData[index] = updatedMessage;
                
                // Update messages
                this._messages.next(this._messagesData);
                
                return of(updatedMessage);
            })
        );
    }

    /**
     * Delete the message
     *
     * @param id
     */
    delete(id: string): Observable<boolean> {
        return this.messages$.pipe(
            take(1),
            switchMap((messages) => {
                // Find the index
                const index = this._messagesData.findIndex(item => item.id === id);
                
                if (index === -1) {
                    return of(false);
                }
                
                // Remove the message
                this._messagesData.splice(index, 1);
                
                // Update messages
                this._messages.next([...this._messagesData]);
                
                return of(true);
            })
        );
    }

    /**
     * Mark all messages as read
     */
    markAllAsRead(): Observable<boolean> {
        return this.messages$.pipe(
            take(1),
            map((messages) => {
                // Mark all as read
                this._messagesData = this._messagesData.map(message => ({
                    ...message,
                    read: true
                }));
                
                // Update messages
                this._messages.next(this._messagesData);
                
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
