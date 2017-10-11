import {Component, Input} from "@angular/core";
import {AuthService} from "../../services/auth.service";
import {ReplaySubject} from "rxjs";
import {Router} from "@angular/router";

@Component({
  selector: 'app-bar',
  templateUrl: './app-bar.component.html',
})
export class AppBar {
  @Input()
  sourceURL: string = null;

  isAuthenticated: ReplaySubject<boolean>;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.isAuthenticated = this.authService.isAuthenticated$;
    this.isAuthenticated.subscribe((isAuth) => {
      console.log('Is auth ', isAuth);
    });
  }

  onLogoutClick() {
    this.authService.logout();
    this.router.navigateByUrl('login');
  }

  onSearchClick(event, searchEntry) {
    event.preventDefault();
    const queryParams = {
      entry: searchEntry
    };
    this.router.navigate(['search'], {queryParams});
  }
}
