import { Injectable } from '@angular/core';
import {AlbumsPage, Album} from '../models/album';
import cloneDeep from 'lodash/cloneDeep';
import find from 'lodash/find';
import sortBy from 'lodash/sortBy';
import reverse from 'lodash/reverse';
import identity from 'lodash/identity';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/map';

import {getAlbums} from './mocked-data';

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

function mapToAlbum(item): Album {
  return {
    id: item.id,
    title: item.title,
    coverImageURL: item.thumb,
    artists: []
  };
}

const headers = new HttpHeaders().set('Authorization', 'Discogs token=uwBGTHIwIUVQftGUciVuEFVFHoyINAYqHjWEHGta');

@Injectable()
export class AlbumService {

  constructor(
    private http: HttpClient
  ) {
  }

  getPage({pageNumber, sortType}: PageRequest): Promise<AlbumsPage> {
    return this.http.get('https://api.discogs.com/database/search', {
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
    return this.http.get(`https://api.discogs.com/releases/${albumId}`, {headers})
      .map((response: any) => {
        console.log(response);
        const album = mapToAlbum(response);
        album.artists = response.artists.map(({id, name}) => ({id, name}));
        return album;
      })
      .toPromise();
  }
}
