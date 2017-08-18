import { Injectable } from '@angular/core';
import {AlbumsPage, Album, Artist, Track} from '../models/album';
import sortBy from 'lodash/sortBy';
import reverse from 'lodash/reverse';
import identity from 'lodash/identity';
import {HttpClient} from "@angular/common/http";

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/map';
import {AppConfig} from "../app.config";

const SORT_HASH = {
  'NO_SORTING': identity,
  'TITLE_ASC': (albums) => sortBy(albums, (album) => album.title),
  'TITLE_DESC': (albums) => reverse(sortBy(albums, (album) => album.title))
};

interface PageRequest {
  pageNumber: number,
  sortType: string
}

function mapToArtist({artist_id, artist_name}): Artist {
  return {
    id: artist_id,
    name: artist_name
  }
}

function mapToTrack({name, duration}): Track {
  return {
    //convert to miliseconds
    duration: `${duration}` + '000',
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
  track_list
}): Album {
  return {
    id: _id,
    title: name,
    coverImageURL: cover_image_url,
    country,
    style,
    format,
    genre,
    trackList: track_list ? track_list.map(mapToTrack): [],
    artists: artist_list.map(mapToArtist)
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
    return `${this.config.apiURL}/mapr-music/api/1.0/albums?page=${pageNumber}`;
  }

/**
 * @desc get albums page from server side
 * */
  getAlbumsPage(request: PageRequest): Promise<AlbumsPage> {
    debugger;
    return this.http.get(this.getAlbumsPageURL(request))
      .map((response: any) => {
        const albums = response.results.map(mapToAlbum);
        return {
          albums: SORT_HASH[request.sortType](albums),
          totalNumber: response.pagination.items
        };
      })
      .toPromise();
  }

/**
 * @desc get album by id URL
 * */
  getAlbumByIdURL(albumId: string): string {
    return `${this.config.apiURL}/mapr-music/api/1.0/albums/${albumId}`;
  }

/**
 * @desc get album by id from server side
 * */
  getAlbumById(albumId: string):Promise<Album> {
    return this.http.get(this.getAlbumByIdURL(albumId))
      .map((response: any) => {
        console.log(response);
        return mapToAlbum(response);
      })
      .toPromise();
  }
}
