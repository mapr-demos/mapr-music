import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";
import has from "lodash/has";
import {SearchResult, SearchResultsPage} from "../models/search-result";

const SEARCHING_URL_HASH = {
  'EVERYTHING': 'name',
  'ALBUMS': 'albums/name',
  'ARTISTS': 'artists/name'
};

const mapToSearchResult = ({
                             _id,
                             type,
                             name,
                             image_url,
                             slug
                           }): SearchResult => ({
  id: _id,
  type,
  name,
  slug,
  imageURL: image_url
});

@Injectable()
export class SearchService {
  private static SERVICE_URL = 'api/1.0/search';
  private static PER_PAGE_DEFAULT = 9;

  constructor(private http: HttpClient,
              private config: AppConfig) {
  }

  getSearchURL(searchType: string, nameEntry: string, page: number): string {
    if (!has(SEARCHING_URL_HASH, searchType)) {
      throw new Error('Unknown search type');
    }
    return `${this.config.apiURL}/${SearchService.SERVICE_URL}/${SEARCHING_URL_HASH[searchType]}?entry=${nameEntry}&page=${page}&per_page=${SearchService.PER_PAGE_DEFAULT}`;
  }

  find(searchType: string, nameEntry: string, page: number): Promise<SearchResultsPage> {
    return this.http.get(this.getSearchURL(searchType, nameEntry, page))
      .map((response: any) => {
        const searchResults = response.results.map(mapToSearchResult);
        return {
          searchResults,
          totalNumber: response.pagination.items
        };
      })
      .toPromise();
  }
}
