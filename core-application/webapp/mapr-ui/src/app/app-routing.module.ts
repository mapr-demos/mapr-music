import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {CommonModule} from "@angular/common";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {HomePage} from "./pages/home-page/home-page.component";
import {AlbumDetailPage} from "./pages/album-detail-page/album-detail-page.component";
import {ArtistPage} from "./pages/artist-page/artist-page.component";
import {NotFoundPage} from "./pages/not-found-page/not-found-page.component";
import {ReportingPage} from "./pages/reporting-page/reporting-page.component";
import {AddAlbumPage} from "./pages/add-album-page/add-album-page.component";
import {EditAlbumPage} from "./pages/edit-album-page/edit-album-page.component";
import {AppBar} from './components/app-bar-component/app-bar.component';
import {AlbumEditForm} from './components/album-edit-form-component/album-edit-form.component';

import { FormsModule }   from '@angular/forms';


const appRoutes: Routes = [
  {
    path: '',
    component: HomePage
  },
  {
    path: 'album/edit/:albumSlug',
    component: EditAlbumPage
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
    path: 'add/album',
    component: AddAlbumPage
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
    ArtistPage,
    AppBar,
    AlbumEditForm,
    AddAlbumPage,
    EditAlbumPage
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
