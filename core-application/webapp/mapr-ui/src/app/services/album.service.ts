import { Injectable } from '@angular/core';
import {AlbumsPage, Album} from '../models/album';
import cloneDeep from 'lodash/cloneDeep';
import find from 'lodash/find';
import sortBy from 'lodash/sortBy';
import reverse from 'lodash/reverse';
import identity from 'lodash/identity';

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

@Injectable()
export class AlbumService {

  getPage({pageNumber, sortType}: PageRequest): Promise<AlbumsPage> {
    const offset = PAGE_SIZE * (pageNumber - 1);
    const albums = getAlbums();
    const sortedAlbums = SORT_HASH[sortType](albums);
    return Promise.resolve({
      albums: cloneDeep(sortedAlbums.slice(offset, offset + PAGE_SIZE)),
      totalNumber: albums.length
    });
  }

  getById(albumId: string):Promise<Album> {
    const albums = getAlbums();
    const album = find(albums, (album) => album.id === albumId);
    if (!album) {
      return Promise.reject('Album not found');
    }
    return Promise.resolve(cloneDeep(album));
  }
}
