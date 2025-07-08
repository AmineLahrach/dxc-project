import { Injectable } from '@angular/core';
import { Chat } from 'app/layout/common/quick-chat/quick-chat.types';
import {
    BehaviorSubject,
    map,
    Observable,
    of,
    switchMap,
    tap,
    throwError,
    take // Add the missing import
} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class QuickChatService {
    private _chat: BehaviorSubject<Chat> = new BehaviorSubject(null);
    private _chats: BehaviorSubject<Chat[]> = new BehaviorSubject<Chat[]>(null);

    // Static chat data
    private _chatsData: Chat[] = [
        {
            id: '9d3f0e7f-dcbd-4e56-a5e8-87b8154e9edf',
            contactId: 'a8991c76-2fda-4bbd-a718-df13d6478847',
            contact: {
                id: 'a8991c76-2fda-4bbd-a718-df13d6478847',
                avatar: 'images/avatars/male-01.jpg',
                name: 'John Doe',
                about: 'Hey there! I am using chat.'
                // Removed status property as it doesn't exist in the Contact interface
            },
            unreadCount: 2,
            muted: false,
            lastMessage: 'Hey, how are you? Do you have time to discuss the project?',
            lastMessageAt: '2023-05-01T12:05:32.000Z'
        },
        {
            id: '8b8d4c43-315d-48da-9dae-f22e8b3ce5b7',
            contactId: 'c1e050cb-76a7-4a67-a242-af80565416f2',
            contact: {
                id: 'c1e050cb-76a7-4a67-a242-af80565416f2',
                avatar: 'images/avatars/female-01.jpg',
                name: 'Jane Smith',
                about: 'Project Manager'
                // Removed status property
            },
            unreadCount: 0,
            muted: true,
            lastMessage: 'The meeting has been rescheduled to tomorrow.',
            lastMessageAt: '2023-04-29T15:32:45.000Z'
        },
        {
            id: '4c8970a6-5dc6-4d6f-a93b-12932eab52ac',
            contactId: '7780af8f-80c1-4d9d-927c-dfa2b8bf335c',
            contact: {
                id: '7780af8f-80c1-4d9d-927c-dfa2b8bf335c',
                avatar: 'images/avatars/male-02.jpg',
                name: 'Michael Johnson',
                about: 'Developer'
                // Removed status property
            },
            unreadCount: 1,
            muted: false,
            lastMessage: 'I\'ve pushed the latest changes to the repository.',
            lastMessageAt: '2023-05-02T09:21:14.000Z'
        }
    ];

    /**
     * Constructor
     */
    constructor() {
        // Initialize with static data
        this._chats.next(this._chatsData);
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for chat
     */
    get chat$(): Observable<Chat> {
        return this._chat.asObservable();
    }

    /**
     * Getter for chat
     */
    get chats$(): Observable<Chat[]> {
        return this._chats.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get chats
     */
    getChats(): Observable<any> {
        // Return static data instead of HTTP call
        return of(this._chatsData).pipe(
            tap((response: Chat[]) => {
                this._chats.next(response);
            })
        );
    }

    /**
     * Get chat
     *
     * @param id
     */
    getChatById(id: string): Observable<any> {
        // Find chat by id from static data
        return of(this._chatsData.find(chat => chat.id === id)).pipe(
            map((chat) => {
                // Update the chat
                this._chat.next(chat);

                // Return the chat
                return chat;
            }),
            switchMap((chat) => {
                if (!chat) {
                    return throwError(() => 
                        new Error('Could not found chat with id of ' + id + '!')
                    );
                }

                return of(chat);
            })
        );
    }

    /**
     * Create a new chat or update existing
     * 
     * @param chat 
     */
    createOrUpdateChat(chat: Chat): Observable<Chat> {
        return this.chats$.pipe(
            take(1),
            switchMap(chats => {
                // Check if chat already exists
                const index = this._chatsData.findIndex(item => item.id === chat.id);
                
                if (index !== -1) {
                    // Update existing chat
                    this._chatsData[index] = chat;
                } else {
                    // Create new chat with a unique ID if not provided
                    const newChat = {
                        ...chat,
                        id: chat.id || this._generateUniqueId()
                    };
                    this._chatsData.push(newChat);
                }
                
                // Update chats
                this._chats.next([...this._chatsData]);
                
                // Return the chat
                return of(chat);
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
