import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { AuthUtils } from 'app/core/auth/auth.utils';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/models/auth.models';
import { BehaviorSubject, catchError, Observable, of, switchMap, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private _authenticated: boolean = false;
    private _httpClient = inject(HttpClient);
    private _userService = inject(UserService);
    private baseUrl: string = 'http://localhost:8081/api/auth/';

    private _currentUser: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);
    public currentUser$ = this._currentUser.asObservable();

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Setter & getter for access token
     */
    set accessToken(token: string) {
        localStorage.setItem('accessToken', token);
    }

    get accessToken(): string {
        return localStorage.getItem('accessToken') ?? '';
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Forgot password
     *
     * @param email
     */
    forgotPassword(email: string): Observable<any> {
        // MOCK IMPLEMENTATION
        console.log('Mock forgot password for email:', email);
        return of({ success: true, message: 'Password reset email sent successfully.' });
        
        // ORIGINAL IMPLEMENTATION - uncomment for production
        // return this._httpClient.post('api/auth/forgot-password', email);
    }

    /**
     * Reset password
     *
     * @param password
     */
    resetPassword(password: string): Observable<any> {
        // MOCK IMPLEMENTATION
        console.log('Mock reset password:', password);
        return of({ success: true, message: 'Password reset successfully.' });
        
        // ORIGINAL IMPLEMENTATION - uncomment for production
        // return this._httpClient.post('api/auth/reset-password', password);
    }

    /**
     * Sign in
     *
     * @param credentials
     */
    signIn(credentials: any): Observable<any> {
        // Throw error, if the user is already logged in
        if (this._authenticated) {
            return throwError(() => new Error('User is already logged in.'));
        }

        // MOCK IMPLEMENTATION
        // console.log('Mock sign in with credentials:', credentials);
        
        // // Basic validation for demo purposes
        // if (credentials.usernameOrEmail && credentials.motDePasse) {
        //     // Create response object similar to backend
        //     const mockResponse = {
        //         token: this._mockToken,
        //         user: this._mockUser
        //     };
            
        //     // Store the access token in local storage
        //     this.accessToken = mockResponse.token;
            
        //     // Set the authenticated flag to true
        //     this._authenticated = true;
        //                        
        //     // Store the user on the user service
        //     this._userService.user = {...mockResponse.user};
            
        //     // Update current user observable
        //     this._currentUser.next(this._mockUser);
            
        //     // Save user to local storage for persistence
        //     localStorage.setItem('currentUser', JSON.stringify(this._mockUser));
            
        //     // Return a new observable with the response
        //     return of(mockResponse);
        // }
        
        // // Invalid credentials
        // return throwError(() => new Error('Invalid credentials'));
        
        // ORIGINAL IMPLEMENTATION - uncomment for production
        
        return this._httpClient.post(`${this.baseUrl}login`, credentials).pipe(
            switchMap((response: any) => {
                // Store the access token in the local storage
                this.accessToken = response.token;
                
                // Set the authenticated flag to true
                
                this._authenticated = true;
                const _user : User = {
                    id: response.id,
                    avatar: response.avatar || 'images/avatars/brian-hughes.jpg',
                    nom: response.nom,
                    prenom: response.prenom,
                    email: response.email,
                    username: response.username,
                    roles: response.roles,
                    serviceLine: response.serviceLine,        
                    actif: true,
                    createdAt: new Date().toISOString(),
                    updatedAt: new Date().toISOString(),
                    status: 'online'
                }
                // Store the user on the user service
                this._userService.user = {..._user};
                // Update current user observable
                this._currentUser.next(_user);
                localStorage.setItem('currentUser', JSON.stringify(_user));
                // Return a new observable with the response
                return of(response);
            })
        );
        
    }

    /**
     * Sign in using the access token (local only, no backend call)
     */
    signInUsingToken(): Observable<any> {
        // Get user from local storage if available
        const storedUser = this.getCurrentUserFromStorage();

        if (storedUser && !AuthUtils.isTokenExpired(this.accessToken)) {
            // Set the authenticated flag to true
            this._authenticated = true;

            // Store the user on the user service
            this._userService.user = { ...storedUser };

            // Update current user observable
            this._currentUser.next(storedUser);

            // Return success
            return of(true);
        }

        // No stored user or token expired, return false
        return of(false);
    }

    /**
     * Sign out
     */
    signOut(): Observable<any> {
        
        localStorage.removeItem('accessToken');
        
        localStorage.removeItem('currentUser');
        this._authenticated = false;
        this._currentUser.next(null);
        return of(true);
    }

    /**
     * Sign up
     *
     * @param user
     */
    signUp(user: {
        name: string;
        email: string;
        password: string;
        company: string;
    }): Observable<any> {
        return this._httpClient.post('api/auth/sign-up', user);
    }

    /**
     * Unlock session
     *
     * @param credentials
     */
    unlockSession(credentials: {
        email: string;
        password: string;
    }): Observable<any> {
        return this._httpClient.post('api/auth/unlock-session', credentials);
    }

    public getCurrentUserFromStorage(): User | null {
        const userStr = localStorage.getItem('currentUser');
        return userStr ? JSON.parse(userStr) : null;
    }

    /**
     * Check the authentication status
     */
    check(): Observable<boolean> {
        // First verify we have the necessary credentials
        if (!this.accessToken) {
            console.log('No access token found');
            this._authenticated = false;
            return of(false);
        }

        // Then check if the token is expired
        if (AuthUtils.isTokenExpired(this.accessToken)) {
            console.log('Token is expired');
            this._authenticated = false;
            return of(false);
        }
        
        // If we have a valid token but _authenticated flag is false,
        // we need to sign in using the token
        if (!this._authenticated) {
            return this.signInUsingToken().pipe(
                catchError(error => {
                    console.error('Error during token authentication:', error);
                    return of(false);
                })
            );
        }
        
        // If we're already authenticated and have a valid token
        return of(true);
    }

    /**
     * Check if user has specific role
     */
    hasRole(role: string): Observable<boolean> {
        return this.currentUser$.pipe(
            switchMap((user) => {
                if (!user) {
                    return of(false);
                }
                return of(user.roles.includes(role));
            })
        );
    }

    /**
     * Check if current user is admin
     */
    isAdmin(): boolean {
        const user = this.getCurrentUserFromStorage() || this._currentUser.value;
        return !!user && Array.isArray(user.roles) && user.roles.includes('ADMINISTRATEUR');
    }

    /**
     * Check if current user is director
     */
    isDirector(): boolean {
        const user = this.getCurrentUserFromStorage() || this._currentUser.value;
        return !!user && Array.isArray(user.roles) && user.roles.includes('ROLE_DIRECTOR');
    }

    /**
     * Check if current user is collaborator
     */
    isCollaborator(): boolean {
        const user = this.getCurrentUserFromStorage() || this._currentUser.value;
        return !!user && Array.isArray(user.roles) && user.roles.includes('ROLE_COLLABORATOR');
    }

    constructor() {
        // Restore token
        const token = localStorage.getItem('accessToken');
        if (token) {
            this.accessToken = token;
        }

        // Restore user
        const userStr = localStorage.getItem('currentUser');
        if (userStr) {
            const user = JSON.parse(userStr);
            this._currentUser.next(user);
        }
    }
}
