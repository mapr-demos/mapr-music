import { Component, OnInit } from '@angular/core';
import {AlbumService} from "../../services/album.service";
import {Album} from '../../models/Album';
import { ActivatedRoute, Router } from '@angular/router';
import 'rxjs/add/operator/switchMap';

@Component({
  selector: 'home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePage implements OnInit{
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private albumService: AlbumService
  ) {}

  albums: Array<Album> = [];
  totalAlbums: number = 0;
  pageNumber: number = 1;

  ngOnInit(): void {
    this.route.queryParams
      .switchMap(({page = 1}) => {
        this.pageNumber = page;
        return this.albumService.getPage(page)
      })
      .subscribe(({albums, totalNumber}) => {
        this.albums = albums;
        this.totalAlbums = totalNumber;
        console.log(this.totalAlbums);
      });
  }

  onChangePage() {
    this.router.navigate(['/'], {queryParams: {page: this.pageNumber}});
  }
}
