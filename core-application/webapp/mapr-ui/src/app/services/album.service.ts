import { Injectable } from '@angular/core';
import {AlbumsPage, Album} from '../models/album';
import sortBy from 'lodash/sortBy';
import reverse from 'lodash/reverse';
import identity from 'lodash/identity';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/map';
import {AppConfig} from "../app.config";

const PAGE_SIZE = 12;

const SORT_HASH = {
  'NO_SORTING': identity,
  'TITLE_ASC': (albums) => sortBy(albums, (album) => album.title),
  'TITLE_DESC': (albums) => reverse(sortBy(albums, (album) => album.title))
};

interface PageRequest {
  pageNumber: number,
  sortType: string
}

function mapToAlbum({id, title, thumb, country}): Album {
  return {
    id,
    title,
    coverImageURL: thumb,
    country,
    artists: []
  };
}

const headers = new HttpHeaders().set('Authorization', 'Discogs token=uwBGTHIwIUVQftGUciVuEFVFHoyINAYqHjWEHGta');

@Injectable()
export class AlbumService {

  constructor(
    private http: HttpClient,
    private config: AppConfig
  ) {
  }

  getPage({pageNumber, sortType}: PageRequest): Promise<AlbumsPage> {
    return this.http.get(`${this.config.apiURL}/database/search`, {
        headers,
        params: new HttpParams()
          .set('type', 'release')
          .set('per_page', `${PAGE_SIZE}`)
          .set('page', `${pageNumber}`)
      })
      .map((response: any) => {
        const albums = response.results.map(mapToAlbum);
        return {
          albums: SORT_HASH[sortType](albums),
          totalNumber: response.pagination.items
        };
      })
      .toPromise();
  }

  getById(albumId: string):Promise<Album> {
    return this.http.get(`${this.config.apiURL}/releases/${albumId}`, {headers})
      .map((response: any) => {
        const album = mapToAlbum(response);
        album.artists = response.artists.map(({id, name}) => ({id, name}));
        return album;
      })
      .toPromise();
  }
}
