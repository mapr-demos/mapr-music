import { Injectable } from '@angular/core';
import {HttpHeaders, HttpClient} from "@angular/common/http";
import {Artist} from "../models/artist";

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/mergeMap';
import {AppConfig} from "../app.config";

const headers = new HttpHeaders().set('Authorization', 'Discogs token=uwBGTHIwIUVQftGUciVuEFVFHoyINAYqHjWEHGta');

function mapToArtist({id, name, images}):Artist {
  return {
    id,
    name,
    gender: 'Male',
    avatarURL: images[0].uri,
    albums: []
  }
}

@Injectable()
export class ArtistService {

  constructor(
    private http: HttpClient,
    private config: AppConfig
  ) {
  }

  getById(artistId: string) {
    return this.http.get(`${this.config.apiURL}/artists/${artistId}`, {headers})
      .map((response: any) => {
        console.log(response);
        return mapToArtist(response);
      })
      .mergeMap((artist: Artist) => {
        return this.http.get(`${this.config.apiURL}/artists/${artist.id}/releases`)
          .map((response: any) => {
            console.log(response.releases);
            artist.albums = response.releases.map(({id, thumb, title}) => ({
              id,
              coverImageURL: thumb,
              title
            }));
            return artist;
          });
      })
      .toPromise();
  }
}
