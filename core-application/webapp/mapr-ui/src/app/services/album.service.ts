import {Injectable} from "@angular/core";
import {AlbumsPage, Album, Artist, Track} from "../models/album";
import identity from "lodash/identity";
import {HttpClient} from "@angular/common/http";
import "rxjs/add/operator/toPromise";
import "rxjs/add/operator/map";
import {AppConfig} from "../app.config";

const PAGE_SIZE = 12;

export const SORT_OPTIONS = [
  {
    label:'No sorting',
    value: 'NO_SORTING'
  },
  {
    label:'Title A-z',
    value: 'TITLE_ASC'
  },
  {
    label:'Title z-A',
    value: 'TITLE_DESC'
  },
  {
    label: 'Newest first',
    value: 'RELEASE_DESC'
  },
  {
    label: 'Oldest first',
    value: 'RELEASE_ASC'
  },
  {
    label: 'Newest First, Title A-z',
    value: 'RELEASE_DESC_TITLE_ASC'
  },
  {
    label: 'Newest First, Title z-A',
    value: 'RELEASE_DESC_TITLE_DESC'
  },
  {
    label: 'Oldest first, Title A-z',
    value: 'RELEASE_ASC_TITLE_ASC'
  },
  {
    label: 'Oldest first, Title z-A',
    value: 'RELEASE_ASC_TITLE_DESC'
  }
];

const SORT_HASH = {
  'NO_SORTING': identity,
  'RELEASE_DESC': (url) => `${url}&sort=desc,released_date`,
  'RELEASE_ASC': (url) => `${url}&sort=asc,released_date`,
  'TITLE_ASC': (url) => `${url}&sort=asc,name`,
  'TITLE_DESC': (url) => `${url}&sort=desc,name`,
  'RELEASE_DESC_TITLE_ASC': (url) => SORT_HASH.TITLE_ASC(SORT_HASH.RELEASE_DESC(url)),
  'RELEASE_DESC_TITLE_DESC': (url) => SORT_HASH.TITLE_DESC(SORT_HASH.RELEASE_DESC(url)),
  'RELEASE_ASC_TITLE_ASC': (url) => SORT_HASH.TITLE_ASC(SORT_HASH.RELEASE_ASC(url)),
  'RELEASE_ASC_TITLE_DESC': (url) => SORT_HASH.TITLE_DESC(SORT_HASH.RELEASE_ASC(url))
};

interface PageRequest {
  pageNumber: number,
  sortType: string
}

function mapToArtist({artist_id, name}): Artist {
  return {
    id: artist_id,
    name
  }
}

function mapToTrack({id, name, length}): Track {
  return {
    id,
    //convert to miliseconds
    duration: `${length}` + '',
    name
  };
}

function mapToAlbum({
  _id,
  name,
  cover_image_url,
  country,
  artist_list,
  style,
  format,
  genre,
  track_list,
  slug
}): Album {
  return {
    id: _id,
    title: name,
    coverImageURL: cover_image_url,
    country,
    style,
    format,
    genre,
    slug,
    trackList: track_list
      ? track_list.map(mapToTrack)
      : [],
    artists: artist_list
      ? artist_list.map(mapToArtist)
      : []
  };
}

@Injectable()
export class AlbumService {

  constructor(
    private http: HttpClient,
    private config: AppConfig
  ) {
  }

/**
 * @desc returns URL for albums page request
 * */
  getAlbumsPageURL({pageNumber, sortType}: PageRequest): string {
    const url = `${this.config.apiURL}/mapr-music/api/1.0/albums?page=${pageNumber}&per_page=${PAGE_SIZE}`;
    return SORT_HASH[sortType](url);
  }

/**
 * @desc get albums page from server side
 * */
  getAlbumsPage(request: PageRequest): Promise<AlbumsPage> {
    return this.http.get(this.getAlbumsPageURL(request))
      .map((response: any) => {
        console.log('Albums: ', response);
        const albums = response.results.map(mapToAlbum);
        return {
          albums,
          totalNumber: response.pagination.items
        };
      })
      .toPromise();
  }

/**
 * @desc get album by slug URL
 * */
  getAlbumBySlugURL(albumSlug: string): string {
    return `${this.config.apiURL}/mapr-music/api/1.0/albums/slug/${albumSlug}`;
  }

/**
 * @desc get album by slug from server side
 * */
  getAlbumBySlug(albumSlug: string):Promise<Album> {
    return this.http.get(this.getAlbumBySlugURL(albumSlug))
      .map((response: any) => {
        console.log('Album: ', response);
        return mapToAlbum(response);
      })
      .toPromise();
  }
}
