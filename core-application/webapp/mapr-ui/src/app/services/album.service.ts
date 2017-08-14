import { Injectable } from '@angular/core';
import {AlbumsPage, Album} from '../models/album';
import cloneDeep from 'lodash/cloneDeep';
import find from 'lodash/find';
import sortBy from 'lodash/sortBy';
import reverse from 'lodash/reverse';
import identity from 'lodash/identity';


const albums: Array<Album> = [];

for (let i = 0; i < 400; i++) {
  albums.push({
    id: `Album_${i}`,
    title: `Test_${i}`,
    coverImageURL: 'https://img.discogs.com/f_1kbeUvSqknkebMjY-5qHd42T4=/300x300/filters:strip_icc():format(jpeg):mode_rgb():quality(40)/discogs-images/R-10684528-1502358314-3248.jpeg.jpg',
    artists: [
      {
        id: 'Artist_1',
        name: 'Test Name'
      }
    ]
  });
}

const PAGE_SIZE = 12;

const SORT_HASH = {
  'NO_SORTING': identity,
  'TITLE_ASC': (albums) => sortBy(albums, (album) => album.title),
  'TITLE_DESC': (albums) => reverse(sortBy(albums, (album) => album.title))
};

@Injectable()
export class AlbumService {

  getPage({pageNumber, sortType}: {pageNumber: number, sortType: string}): Promise<AlbumsPage> {
    const offset = PAGE_SIZE * (pageNumber - 1);
    const sortedAlbums = SORT_HASH[sortType](albums);
    return Promise.resolve({
      albums: cloneDeep(sortedAlbums.slice(offset, offset + PAGE_SIZE)),
      totalNumber: albums.length
    });
  }

  getById(albumId: string):Promise<Album> {
    const album = find(albums, (album) => album.id === albumId);
    if (!album) {
      return Promise.reject('Album not found');
    }
    return Promise.resolve(album);
  }
}
