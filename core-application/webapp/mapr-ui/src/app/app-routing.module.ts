import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {CommonModule} from "@angular/common";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {HomePage} from "./pages/home-page/home-page.component";
import {AlbumDetailPage} from "./pages/album-detail-page/album-detail-page.component";
import {ArtistPage} from "./pages/artist-page/artist-page.component";
import {NotFoundPage} from "./pages/not-found-page/not-found-page.component";
import {ReportingPage} from "./pages/reporting-page/reporting-page.component";
import { FormsModule }   from '@angular/forms';


const appRoutes: Routes = [
  {
    path: '',
    component: HomePage
  },
  {
    path: 'album/:albumSlug',
    component: AlbumDetailPage
  },
  {
    path: 'reporting',
    component: ReportingPage
  },
  {
    path: 'artist/:artistId',
    component: ArtistPage
  },
  {
    path: '**',
    component: NotFoundPage
  }
];

@NgModule({
  declarations: [
    HomePage,
    NotFoundPage,
    ReportingPage,
    AlbumDetailPage,
    ArtistPage
  ],
  imports: [
    FormsModule,
    NgbModule,
    CommonModule,
    RouterModule.forRoot(appRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule{}
