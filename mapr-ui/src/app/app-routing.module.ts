import {NgModule} from '@angular/core';
import { RouterModule, Routes }  from '@angular/router';
import { CommonModule } from '@angular/common'
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { HomePage } from './pages/home-page/home-page.component';
import { AlbumDetailPage } from './pages/album-detail-page/album-detail-page.component';
import { NotFoundPage } from './pages/not-found-page/not-found-page.component';

const appRoutes: Routes = [
  {
    path: '',
    component: HomePage
  },
  {
    path: 'album/:albumId',
    component: AlbumDetailPage
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
    AlbumDetailPage
  ],
  imports: [
    NgbModule,
    CommonModule,
    RouterModule.forRoot(appRoutes)
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule{}
