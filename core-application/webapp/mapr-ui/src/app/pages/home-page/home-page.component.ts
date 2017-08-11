import { Component, OnInit } from '@angular/core';
import {AlbumService} from "../../services/album.service";
import {Album} from '../../models/Album';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbPaginationConfig } from '@ng-bootstrap/ng-bootstrap';
import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css'],
})
export class HomePage implements OnInit{
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private albumService: AlbumService,
    private config: NgbPaginationConfig
  ) {
    config.maxSize = 5;
  }

  albums: Array<Album> = [];
  totalAlbums: number;
  pageNumber: number;

  ngOnInit(): void {
    this.route.queryParams
      .switchMap(({page = 1}) => {
        return this.albumService.getPage(page).then((albumsPage) => [albumsPage, page]);
      })
      .subscribe(([{albums, totalNumber}, page]) => {
        this.pageNumber = page;
        this.albums = albums;
        this.totalAlbums = totalNumber;
      });
  }

  onChangePage(newPage: number) {
    if (!Number.isNaN(newPage)) {
      this.pageNumber = newPage;
      this.router.navigate(['/'], {queryParams: {page: this.pageNumber}});
    }
  }
}
