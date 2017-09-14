import {Component} from "@angular/core";
import "rxjs/add/operator/switchMap";
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'login-page',
  templateUrl: './login-page.component.html'
})
export class LoginPage {
  login: string = '';
  pass: string = '';

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private authService: AuthService
  ) {
  }

  onLoginClick() {
    this.authService.auth(this.login, this.pass).then((isAuthenticated) => {
      if (isAuthenticated) {
        const {returnUrl} = this.activatedRoute.snapshot.queryParams;
        if (!returnUrl) {
          this.router.navigateByUrl('');
          return;
        }
        this.router.navigateByUrl(returnUrl);
      }
    });
  }
}
