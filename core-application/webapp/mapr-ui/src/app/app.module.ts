import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {HttpClientModule, HTTP_INTERCEPTORS} from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { AlbumService } from './services/album.service';
import { ArtistService } from './services/artist.service';
import { AppConfig } from "./app.config";
import {LanguageService} from "./services/language.service";
import {AuthService} from "./services/auth.service";
import {AuthenticatedGuard} from "./guards/authenticated.guard";
import {AuthInterceptor} from "./interceptors/auth.interceptor";
import {ReportingService} from "./services/reporting.service";
import {SearchService} from "./services/search.service";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    NgbModule.forRoot()
  ],
  providers: [
    ArtistService,
    AlbumService,
    SearchService,
    AppConfig,
    LanguageService,
    AuthService,
    AuthenticatedGuard,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
    ReportingService
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
