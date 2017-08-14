import { Injectable } from '@angular/core';
import cloneDeep from 'lodash/cloneDeep';
import find from 'lodash/find';
import {getArtists} from './mocked-data';


@Injectable()
export class ArtistService {
  getById(artistId: string) {
    const artists = getArtists();
    const artist = find(artists, (artist) => artist.id === artistId);
    if (!artist) {
      return Promise.reject('Artist not found');
    }
    return Promise.resolve(cloneDeep(artist));
  }
}
