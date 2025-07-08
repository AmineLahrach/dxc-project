// src/app/core/auth/services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { LoginRequest, JwtResponse, User, SignupRequest } from '../../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private _currentUser: BehaviorSubject<User | null> = new BehaviorSubject(null);
  private _isAuthenticated: BehaviorSubject<boolean> = new BehaviorSubject(false);

  constructor(private _httpClient: HttpClient) {
    this.checkAuthenticationStatus();
  }

  // Getters
  get currentUser$(): Observable<User | null> {
    return this._currentUser.asObservable();
  }

  get isAuthenticated$(): Observable<boolean> {
    return this._isAuthenticated.asObservable();
  }

  get currentUser(): User | null {
    return this._currentUser.value;
  }

  get accessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  // Authentication methods
  login(credentials: LoginRequest): Observable<JwtResponse> {
    return this._httpClient.post<JwtResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(
        map((response: JwtResponse) => {
          this.setSession(response);
          return response;
        }),
        catchError(error => throwError(error))
      );
  }

  register(userData: SignupRequest): Observable<any> {
    return this._httpClient.post(`${environment.apiUrl}/auth/register`, userData);
  }

  logout(): Observable<any> {
    return this._httpClient.post(`${environment.apiUrl}/auth/logout`, {})
      .pipe(
        map(() => {
          this.clearSession();
        }),
        catchError(() => {
          this.clearSession();
          return throwError('Logout failed');
        })
      );
  }

  getCurrentUser(): Observable<User> {
    return this._httpClient.get<User>(`${environment.apiUrl}/auth/me`);
  }

  // Token management
  private setSession(authResult: JwtResponse): void {
    localStorage.setItem('accessToken', authResult.token);
    localStorage.setItem('currentUser', JSON.stringify({
      id: authResult.id,
      username: authResult.username,
      email: authResult.email,
      nom: authResult.nom,
      prenom: authResult.prenom,
      roles: authResult.roles,
      serviceLine: authResult.serviceLine
    }));

    this._currentUser.next(this.getCurrentUserFromStorage());
    this._isAuthenticated.next(true);
  }

  private clearSession(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('currentUser');
    this._currentUser.next(null);
    this._isAuthenticated.next(false);
  }

  private checkAuthenticationStatus(): void {
    const token = this.accessToken;
    const user = this.getCurrentUserFromStorage();

    if (token && user && !this.isTokenExpired(token)) {
      this._currentUser.next(user);
      this._isAuthenticated.next(true);
    } else {
      this.clearSession();
    }
  }

  private getCurrentUserFromStorage(): User | null {
    const userStr = localStorage.getItem('currentUser');
    return userStr ? JSON.parse(userStr) : null;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp < Date.now() / 1000;
    } catch {
      return true;
    }
  }

  // Role checking methods
  hasRole(role: string): boolean {
    const user = this.currentUser;
    return user?.roles?.includes(`ROLE_${role.toUpperCase()}`) || false;
  }

  isAdmin(): boolean {
    return this.hasRole('ADMINISTRATEUR');
  }

  isCollaborator(): boolean {
    return this.hasRole('COLLABORATEUR');
  }

  isDirector(): boolean {
    return this.hasRole('DIRECTEUR_GENERAL');
  }
}