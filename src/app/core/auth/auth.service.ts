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
    private baseUrl: string = 'http://localhost:8081/api/auth';

    private _currentUser: BehaviorSubject<User | null> = new BehaviorSubject<User | null>(null);
    public currentUser$ = this._currentUser.asObservable();

    // Static mock data for when backend is not available
    private readonly _mockUser: User = {
        id: '1',
        avatar: 'img/brian-hughes.jpg',
        nom: 'Admin',
        prenom: 'Super',
        email: 'admin@example.com',
        username: 'admin',
        roles: [],
        serviceLine: "Technologies de l'Information",
        actif: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        status: 'online'
    };

    private readonly _mockToken: string =
        'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1MTk1MTcwNiwiZXhwIjoxNzUyMDM4MTA2fQ.sqxEJrdCc-KowbODIEdck7bSRg0z0vPK4cFBI0UsHvCtsPlivVSp-ULIbEpQia9a71s6UdvJBBgg-BoC3IjjBw';

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
        
        return this._httpClient.post(`${this.baseUrl}/login`, credentials).pipe(
            switchMap((response: any) => {
                // Store the access token in the local storage
                this.accessToken = response.token;
                
                // Set the authenticated flag to true
                
                this._authenticated = true;
                const _user : User = {
                    id: response.id,
                    avatar: response.avatar || 'images/avatars/default-avatar.png',
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
     * Sign in using the access token
     */
    signInUsingToken(): Observable<any> {
        // MOCK IMPLEMENTATION
        console.log('Mock sign in with token');
        
        // Get user from local storage if available
        const storedUser = this.getCurrentUserFromStorage();
        
        if (storedUser) {
            // Set the authenticated flag to true
            this._authenticated = true;
            
            // Store the user on the user service
            this._userService.user = { nom: storedUser.nom, ...storedUser };
            
            // Update current user observable
            this._currentUser.next(storedUser);
            
            // Return success
            return of(true);
        }
        
        // No stored user, return false
        return of(false);
        
        // ORIGINAL IMPLEMENTATION - uncomment for production
        /*
        // Sign in using the token
        return this._httpClient
            .post('api/auth/sign-in-with-token', {
                accessToken: this.accessToken,
            })
            .pipe(
                catchError(() =>
                    // Return false
                    of(false)
                ),
                switchMap((response: any) => {
                    // Replace the access token with the new one if it's available on
                    // the response object.
                    //
                    // This is an added optional step for better security. Once you sign
                    // in using the token, you should generate a new one on the server
                    // side and attach it to the response object. Then the following
                    // piece of code can replace the token with the refreshed one.
                    if (response.accessToken) {
                        this.accessToken = response.accessToken;
                    }

                    // Set the authenticated flag to true
                    this._authenticated = true;

                    // Store the user on the user service
                    this._userService.user = response.user;
                    // Update current user observable
                    this._currentUser.next(response.user);

                    // Return true
                    return of(true);
                })
            );
        */
    }

    /**
     * Sign out
     */
    signOut(): Observable<any> {
        // Remove the access token from the local storage
        localStorage.removeItem('accessToken');
        
        // Remove stored user
        localStorage.removeItem('currentUser');

        // Set the authenticated flag to false
        this._authenticated = false;
        this._currentUser.next(null);

        // Return the observable
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
        // MOCK IMPLEMENTATION
        console.log('Mock sign up with user:', user);
        return of({ success: true, message: 'User registered successfully.' });
        
        // ORIGINAL IMPLEMENTATION - uncomment for production
        // return this._httpClient.post('api/auth/sign-up', user);
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
        // MOCK IMPLEMENTATION
        console.log('Mock unlock session with credentials:', credentials);
        return of({ success: true });
        
        // ORIGINAL IMPLEMENTATION - uncomment for production
        // return this._httpClient.post('api/auth/unlock-session', credentials);
    }

    public getCurrentUserFromStorage(): User | null {
        const userStr = localStorage.getItem('currentUser');
        return userStr ? JSON.parse(userStr) : null;
    }

    /**
     * Check the authentication status
     */
    check(): Observable<boolean> {
        // Check if the user is logged in
        if (this._authenticated) {
            return of(true);
        }

        // Check the access token availability
        if (!this.accessToken) {
            return of(false);
        }

        // MOCK IMPLEMENTATION - Skip token expiration check for development
        // return of(true);
        
        // For better simulation, we'll still use the token expiration check
        // Check the access token expire date
        if (AuthUtils.isTokenExpired(this.accessToken)) {
            return of(false);
        }

        // If the access token exists, and it didn't expire, sign in using it
        return this.signInUsingToken();
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
        return !!user && Array.isArray(user.roles) && user.roles.includes('ROLE_ADMIN');
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
