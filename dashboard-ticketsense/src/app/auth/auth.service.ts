import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, map, catchError, of, switchMap } from 'rxjs';

export interface UserInfo {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  isActive: boolean;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = '/api';
  readonly isAuthenticated = signal(false);
  readonly user = signal<UserInfo | null>(null);

  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string) {
    return this.http
      .post(`${this.apiUrl}/login`, { username, password }, { responseType: 'text', withCredentials: true })
      .pipe(
        switchMap(() => this.http.get<UserInfo>(`${this.apiUrl}/me`, { withCredentials: true })),
        tap(user => {
          this.user.set(user);
          this.isAuthenticated.set(true);
        })
      );
  }

  checkSession(): Observable<boolean> {
    return this.http.get<UserInfo>(`${this.apiUrl}/me`, { withCredentials: true }).pipe(
      tap(user => {
        this.user.set(user);
        this.isAuthenticated.set(true);
      }),
      map(() => true),
      catchError(() => {
        this.isAuthenticated.set(false);
        this.user.set(null);
        return of(false);
      })
    );
  }

  logout() {
    this.http.post(`${this.apiUrl}/logout`, {}, { responseType: 'text', withCredentials: true }).subscribe({
      complete: () => {
        this.isAuthenticated.set(false);
        this.user.set(null);
        this.router.navigate(['/login']);
      },
      error: () => {
        this.isAuthenticated.set(false);
        this.user.set(null);
        this.router.navigate(['/login']);
      }
    });
  }
}
