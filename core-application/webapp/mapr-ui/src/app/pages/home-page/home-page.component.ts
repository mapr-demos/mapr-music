import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import "rxjs/add/operator/switchMap";
import {Album, AlbumsPage, Language} from "../../models/album";
import {AlbumService, SORT_OPTIONS} from "../../services/album.service";
import {SelectOption} from "../../models/select-option";
import {LanguageService} from "../../services/language.service";

interface QueryParams {
  page:number,
  sort: string,
  lang: string
}

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
    private languageService: LanguageService
  ) {
  }

  albums: Array<Album> = [];
  totalAlbums: number;
  pageNumber: number;
  sourceURL: string;
  sortOptions: Array<SelectOption> = SORT_OPTIONS;
  languageOptions: Array<Language>;
  selectedLanguageCode: string;
  sortType: string;

  ngOnInit(): void {
    this.languageService.getAllLanguages()
      .then((languages) => {
        this.languageOptions = languages;
      });
    this.route.queryParams
      .switchMap(({page = 1, sort = 'NO_SORTING', lang = null}: QueryParams) => {
        this.sortType = sort;
        this.selectedLanguageCode = lang;
        this.albums = [];
        const request = {pageNumber: page, sortType: this.sortType, lang};
        this.sourceURL = this.albumService.getAlbumsPageURL(request);
        return this.albumService.getAlbumsPage(request)
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
      lang: this.selectedLanguageCode,
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

  onLanguageChange(languageCode: string) {
    // console.log(languageCode);
    this.selectedLanguageCode = languageCode === 'null'
      ? null
      : languageCode;
    this.changeURL();
  }

  onSortChange(sortType: string) {
    this.sortType = sortType;
    this.changeURL();
  }
}
