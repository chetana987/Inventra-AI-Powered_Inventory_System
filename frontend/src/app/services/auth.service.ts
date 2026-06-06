import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, LoginData, LoginRequest, PasswordChangeRequest, ProfileUpdateRequest, RegisterData, RegisterRequest, UserProfile } from '../models/auth-response';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'inventra_token';
  private readonly USER_KEY = 'inventra_user';
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private rememberMe = true;

  constructor(private http: HttpClient) {
    this.rememberMe = localStorage.getItem('inventra_remember') !== 'false';
    if (!this.rememberMe && this.getToken()) {
      this.migrateToSession();
    }
  }

  private get storage(): Storage {
    return this.rememberMe ? localStorage : sessionStorage;
  }

  login(credentials: LoginRequest, remember?: boolean): Observable<ApiResponse<LoginData>> {
    if (remember !== undefined) this.setRememberMe(remember);
    return this.http.post<ApiResponse<LoginData>>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => this.storeSession(res.data))
    );
  }

  register(data: RegisterRequest): Observable<ApiResponse<RegisterData>> {
    return this.http.post<ApiResponse<RegisterData>>(`${this.apiUrl}/register`, data).pipe(
      tap(res => this.storeSession({ token: res.data.token, email: res.data.email, role: res.data.role }))
    );
  }

  private setRememberMe(val: boolean): void {
    this.rememberMe = val;
    localStorage.setItem('inventra_remember', String(val));
    if (val) this.migrateToLocal();
    else this.migrateToSession();
  }

  private storeSession(data: { token: string; email: string; role: string; name?: string }): void {
    this.storage.setItem(this.TOKEN_KEY, data.token);
    this.storage.setItem(this.USER_KEY, JSON.stringify({ email: data.email, role: data.role, name: data.name }));
  }

  logout(): void {
    this.storage.removeItem(this.TOKEN_KEY);
    this.storage.removeItem(this.USER_KEY);
  }

  getUser(): { email: string; role: string; name?: string } | null {
    try {
      const raw = this.storage.getItem(this.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch { return null; }
  }

  getToken(): string | null {
    return this.storage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  getRememberMe(): boolean {
    return this.rememberMe;
  }

  getProfile(): Observable<ApiResponse<UserProfile>> {
    return this.http.get<ApiResponse<UserProfile>>(`${this.apiUrl.replace('/auth', '/users')}/me`);
  }

  updateProfile(data: ProfileUpdateRequest): Observable<ApiResponse<UserProfile>> {
    return this.http.put<ApiResponse<UserProfile>>(`${this.apiUrl.replace('/auth', '/users')}/profile`, data).pipe(
      tap(res => {
        const current = this.getUser();
        if (current) {
          this.storage.setItem(this.USER_KEY, JSON.stringify({
            ...current,
            name: res.data.name,
            email: res.data.email
          }));
        }
      })
    );
  }

  changePassword(data: PasswordChangeRequest): Observable<ApiResponse<null>> {
    return this.http.put<ApiResponse<null>>(`${this.apiUrl.replace('/auth', '/users')}/password`, data);
  }

  private migrateToSession(): void {
    const t = localStorage.getItem(this.TOKEN_KEY);
    const u = localStorage.getItem(this.USER_KEY);
    if (t) { sessionStorage.setItem(this.TOKEN_KEY, t); localStorage.removeItem(this.TOKEN_KEY); }
    if (u) { sessionStorage.setItem(this.USER_KEY, u); localStorage.removeItem(this.USER_KEY); }
  }

  private migrateToLocal(): void {
    const t = sessionStorage.getItem(this.TOKEN_KEY);
    const u = sessionStorage.getItem(this.USER_KEY);
    if (t) { localStorage.setItem(this.TOKEN_KEY, t); sessionStorage.removeItem(this.TOKEN_KEY); }
    if (u) { localStorage.setItem(this.USER_KEY, u); sessionStorage.removeItem(this.USER_KEY); }
  }
}
