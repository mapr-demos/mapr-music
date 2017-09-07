import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {HttpClientModule} from "@angular/common/http";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AppComponent} from "./app.component";
import {AppRoutingModule} from "./app-routing.module";
import {AlbumService} from "./services/album.service";
import {ArtistService} from "./services/artist.service";
import {AppConfig} from "./app.config";


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
    AlbumService,
    ArtistService,
    AppConfig
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
