import {Injectable} from "@angular/core";
import {ReplaySubject} from "rxjs";
import {AppConfig} from "../app.config";

@Injectable()
export class AuthService {

  private token: string = localStorage.getItem('AUTH_TOKEN');

  isAuthenticated: boolean = this.token !== null;

  isAuthenticated$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private config: AppConfig
  ) {
    this.isAuthenticated$.next(this.isAuthenticated);
    this.isAuthenticated$.subscribe((val) => {
      this.isAuthenticated = val;
    });
  }

  auth(login: string, pass: string): Promise<boolean> {
    this.token = btoa(`${login}:${pass}`);
    const headers = new Headers();
    headers.set('Authorization', this.getAuthHeader());
    return fetch(`${this.config.apiURL}/api/1.0/users/current`, {
      method: 'GET',
      headers
    })
      .then((res) => {
        if (res.ok) {
          localStorage.setItem('AUTH_TOKEN', this.token);
        }
        this.isAuthenticated$.next(res.ok);
        return res.ok;
      });
  }

  getAuthHeader(): string {
    return `Basic ${this.token}`;
  }

  logout(): Promise<void> {
    localStorage.removeItem('AUTH_TOKEN');
    this.isAuthenticated$.next(false);
    return Promise.resolve();
  }
}
