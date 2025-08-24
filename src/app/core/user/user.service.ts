import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SignupRequest, User } from 'app/models/auth.models';
import { map, Observable, ReplaySubject, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {
    private _apiUrl = 'http://localhost:8081/api/users'; // Update this URL
    private _httpClient = inject(HttpClient);
    private _user: ReplaySubject<User> = new ReplaySubject<User>(1);

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Setter & getter for user
     *
     * @param value
     */
    set user(value: User) {
        // Store the value
        this._user.next(value);
    }

    get user$(): Observable<User> {
        return this._user.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get the current signed-in user data
     */
    get(): Observable<User> {
        return this._httpClient.get<User>('api/common/user').pipe(
            tap((user) => {
                this._user.next(user);
            })
        );
    }

    /**
     * Update the user
     *
     * @param user
     */
    update(user: User): Observable<any> {
        return this._httpClient.patch<User>('api/common/user', { user }).pipe(
            map((response) => {
                this._user.next(response);
            })
        );
    }

    getUsers(): Observable<User[]> {
        return this._httpClient.get<User[]>(this._apiUrl);
    }

    getUser(id: string): Observable<User> {
        return this._httpClient.get<User>(`${this._apiUrl}/${id}`);
    }

    createUser(user: SignupRequest): Observable<User> {
        return this._httpClient.post<User>(this._apiUrl, user);
    }

    updateUser(id: string, user: Partial<User>): Observable<User> {
        return this._httpClient.put<User>(`${this._apiUrl}/${id}`, user);
    }

  updateUserStatus(id: string, active: boolean): Observable<any> {
    return this._httpClient.patch<any>(`${this._apiUrl}/${id}/status`, { actif: active });
  }

  deleteUser(id: string): Observable<any> {
    return this._httpClient.delete<any>(`${this._apiUrl}/${id}`);
  }

  bulkDeleteUsers(ids: string[]): Observable<any> {
    return this._httpClient.post<any>(`${this._apiUrl}/bulk-delete`, { ids });
  }
  updateUserProfile(id: string, user: Partial<User>): Observable<User> {
        return this._httpClient.put<User>(`${this._apiUrl}/${id}/profile`, user);
    }
}
