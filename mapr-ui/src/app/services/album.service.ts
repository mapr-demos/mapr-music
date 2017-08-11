import { Injectable } from '@angular/core';
import {AlbumsPage, Album} from '../models/Album';
import cloneDeep from 'lodash/cloneDeep';
import find from 'lodash/find';

const albums = [];

for (let i = 0; i < 400; i++) {
  albums.push(
    new Album(
      `Album_${i}`,
      `Test_${i}`,
      'https://img.discogs.com/f_1kbeUvSqknkebMjY-5qHd42T4=/300x300/filters:strip_icc():format(jpeg):mode_rgb():quality(40)/discogs-images/R-10684528-1502358314-3248.jpeg.jpg'
    ));
}

const PAGE_SIZE = 12;

@Injectable()
export class AlbumService {

  getPage(pageNumber: number): Promise<AlbumsPage> {
    const offset = PAGE_SIZE * (pageNumber - 1);
    return Promise.resolve({
      albums: cloneDeep(albums.slice(offset, offset + PAGE_SIZE)),
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
