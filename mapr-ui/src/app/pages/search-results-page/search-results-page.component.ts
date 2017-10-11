import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, Router} from "@angular/router";
import "rxjs/add/operator/switchMap";
import {SearchService} from "../../services/search.service";

@Component({
  selector: 'search-results-page',
  templateUrl: './search-results-page.component.html'
})
export class SearchResultsPage implements OnInit {
  searchOptions = [
    {
      value: 'EVERYTHING',
      label: 'Everything'
    },
    {
      value: 'ALBUMS',
      label: 'Albums'
    },
    {
      value: 'ARTISTS',
      label: 'Artists'
    }
  ];

  selectedSearchOption = null;
  searchResults = null;
  entry = null;
  sourceURL = null;
  totalNumber: number;
  pageNumber: number;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private searchService: SearchService) {
  }

  ngOnInit(): void {
    this.activatedRoute.queryParams
      .switchMap(({type = 'EVERYTHING', entry, page = 1}) => {
        this.selectedSearchOption = type;
        this.entry = entry;
        this.sourceURL = this.searchService.getSearchURL(type, entry, page);
        return this.searchService.find(type, entry, page).then((searchResultsPage: SearchResultsPage) => ({
          searchResultsPage,
          page
        }));
      })
      .subscribe(({searchResultsPage, page}) => {
        const {searchResults, totalNumber} = searchResultsPage;
        this.pageNumber = page;
        this.searchResults = searchResults;
        console.log(searchResults);
        this.totalNumber = totalNumber;
      });

  }

  onSelectSearchOption(event, optionValue) {
    event.preventDefault();
    const queryParams = {
      type: optionValue,
      entry: this.entry
    };
    this.router.navigate(['search'], {queryParams});
  }

  changeURL() {
    const queryParams = {
      type: this.selectedSearchOption,
      entry: this.entry,
      page: this.pageNumber
    };

    this.router.navigate(['/search/'], {queryParams});
  }

  onChangePage(newPage: number) {
    if (!Number.isNaN(newPage)) {
      this.pageNumber = newPage;
      this.changeURL();
    }
  }

}
