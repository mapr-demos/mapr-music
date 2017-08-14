import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import 'rxjs/add/operator/switchMap';

import {Album, AlbumsPage} from '../../models/album';
import {AlbumService} from "../../services/album.service";
import {SelectOption} from '../../models/select-option';

@Component({
  selector: 'home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css'],
})
export class HomePage implements OnInit{
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private albumService: AlbumService
  ) {
  }

  albums: Array<Album> = [];
  totalAlbums: number;
  pageNumber: number;
  sortOptions: Array<SelectOption> = [
    {
      label:'Title A-z',
      value: 'TITLE_ASC'
    },
    {
      label:'Title z-A',
      value: 'TITLE_DESC'
    }
  ];
  sortType: string;

  ngOnInit(): void {
    this.route.queryParams
      .switchMap(({page = 1, sort = 'NO_SORTING'}: {page:number, sort: string}) => {
        this.sortType = sort;
        return this.albumService.getPage({pageNumber: page, sortType: this.sortType})
          .then((albumsPage: AlbumsPage) => ({albumsPage, page}));
      })
      .subscribe(({albumsPage, page}) => {
        const {albums, totalNumber} = albumsPage;
        this.pageNumber = page;
        this.albums = albums;
        this.totalAlbums = totalNumber;
      });
  }

  changeURL() {
    const queryParams = {
      page: this.pageNumber,
      sort: null
    };
    if (this.sortType !== 'NO_SORTING') {
      queryParams.sort = this.sortType;
    }
    this.router.navigate(['/'], {queryParams});
  }

  onChangePage(newPage: number) {
    if (!Number.isNaN(newPage)) {
      this.pageNumber = newPage;
      this.changeURL();
    }
  }

  onSortChange(sortType: string) {
    this.sortType = sortType;
    this.changeURL();
  }
}
