import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Album, Artist, ArtistsPage} from "../models/artist";
import "rxjs/add/operator/map";
import "rxjs/add/operator/toPromise";
import "rxjs/add/operator/mergeMap";
import {AppConfig} from "../app.config";

const PAGE_SIZE = 12;

function mapToArtist({_id, name, profile_image_url, gender, slug, area, disambiguation_comment, begin_date, end_date, IPI, ISNI}): Artist {
  return {
    id: _id,
    name,
    gender,
    avatarURL: profile_image_url,
    slug,
    area,
    disambiguationComment: disambiguation_comment,
    beginDate: (begin_date) ? new Date(begin_date).toDateString() : null,
    endDate: (end_date) ? new Date(end_date).toDateString() : null,
    IPI,
    ISNI,
    albums: []
  }
}

function mapToAlbum({_id, name, cover_image_url, slug}): Album {
  return {
    id: _id,
    title: name,
    slug,
    coverImageURL: cover_image_url
  };
}

@Injectable()
export class ArtistService {

  private static SERVICE_URL = '/api/1.0/artists';

  constructor(private http: HttpClient,
              private config: AppConfig) {
  }

  getArtistByIdURL(artistId: string): string {
    return `${this.config.apiURL}${ArtistService.SERVICE_URL}/${artistId}`;
  }

  getArtistById(artistId: string): Promise<Artist> {
    return this.http.get(this.getArtistByIdURL(artistId))
      .map((response: any) => {
        console.log('Artist: ', response);
        const artist = mapToArtist(response);
        artist.albums = response.albums
          ? response.albums.map(mapToAlbum)
          : [];
        return artist;
      })
      .toPromise();
  }

  getArtistPageURL(pageNum: number): string {
    return `${this.config.apiURL}${ArtistService.SERVICE_URL}?page=${pageNum}&per_page=${PAGE_SIZE}`;
  }

  /**
   * @desc get albums page from server side
   * */
  getArtistPage(pageNum: number): Promise<ArtistsPage> {
    return this.http.get(this.getArtistPageURL(pageNum))
      .map((response: any) => {
        const artists = response.results.map(mapToArtist);
        return {
          artists,
          totalNumber: response.pagination.items
        };
      })
      .toPromise();
  }

  /**
   * @desc get album by slug URL
   * */
  getArtistBySlugURL(artistSlug: string): string {
    return `${this.config.apiURL}${ArtistService.SERVICE_URL}/slug/${artistSlug}`;
  }

  /**
   * @desc get album by slug from server side
   * */
  getArtistBySlug(artistSlug: string): Promise<Artist> {
    return this.http.get(this.getArtistBySlugURL(artistSlug))
      .map((response: any) => {
        console.log('Artist: ', response);
        const artist = mapToArtist(response);
        artist.albums = response.albums
          ? response.albums.map(mapToAlbum)
          : [];
        return artist;
      })
      .toPromise();
  }
}
