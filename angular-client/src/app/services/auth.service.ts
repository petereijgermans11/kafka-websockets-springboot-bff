import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface UserInfo {
  authenticated: boolean;
  sub: string;
  email: string;
  name: string;
  username: string;
}

// Relatieve URLs — Angular dev proxy (proxy.conf.json) stuurt door naar nginx BFF gateway.
// Cookies worden zo op dezelfde origin (localhost:4200) gezet, net als de greeter-app.
const GATEWAY = '';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private readonly http: HttpClient) {}

  getUser(): Observable<UserInfo | null> {
    return this.http
      .get<UserInfo>(`${GATEWAY}/api/user`, { withCredentials: true })
      .pipe(catchError(() => of(null)));
  }

  login(): void {
    window.location.href = `${GATEWAY}/login`;
  }

  logout(): void {
    window.location.href = `${GATEWAY}/logout`;
  }
}
