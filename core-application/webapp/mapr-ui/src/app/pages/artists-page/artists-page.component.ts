import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import {Artist, ArtistsPage} from "../../models/artist";
import {ArtistService} from "../../services/artist.service";
import "rxjs/add/operator/switchMap";

interface QueryParams {
  page:number,
  sort: string,
  lang: string
}

@Component({
  selector: 'artists-page',
  templateUrl: './artists-page.component.html',
  styleUrls: ['./artists-page.component.css'],
})
export class ArtistsPageComponent implements OnInit{
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private artistService: ArtistService
  ) {
  }

  artists: Array<Artist> = [];
  totalAlbums: number;
  pageNumber: number;
  sourceURL: string;

  ngOnInit(): void {

    this.route.queryParams
      .switchMap(({page = 1}: QueryParams) => {
        this.artists = [];
        this.sourceURL = this.artistService.getArtistPageURL(page);
        return this.artistService.getArtistPage(page)
          .then((artistsPage: ArtistsPage) => ({artistsPage, page}));
      })
      .subscribe(({artistsPage, page}) => {
        const {artists, totalNumber} = artistsPage;
        this.pageNumber = page;
        this.artists = artists;
        console.log(artists);
        this.totalAlbums = totalNumber;
      });
  }

  changeURL() {
    const queryParams = {
      page: this.pageNumber
    };

    this.router.navigate(['/artists/'], {queryParams});
  }

  onChangePage(newPage: number) {
    if (!Number.isNaN(newPage)) {
      this.pageNumber = newPage;
      this.changeURL();
    }
  }
}
