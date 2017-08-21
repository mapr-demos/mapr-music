import { Injectable } from '@angular/core';
import {HttpHeaders, HttpClient} from "@angular/common/http";
import {Artist, Album} from "../models/artist";

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/mergeMap';
import {AppConfig} from "../app.config";
import {AlbumService} from "./album.service";

function mapToArtist({_id, name, profile_image_url, gender}):Artist {
  return {
    id: _id,
    name,
    gender,
    avatarURL: profile_image_url,
    albums: []
  }
}

function mapToAlbum({_id, name, cover_image_url}): Album {
  return {
    id: _id,
    title: name,
    coverImageURL: cover_image_url
  };
}

@Injectable()
export class ArtistService {

  constructor(
    private http: HttpClient,
    private config: AppConfig,
    private albumService: AlbumService
  ) {
  }

  getArtistByIdURL(artistId: string): string {
    return `${this.config.apiURL}/mapr-music/api/1.0/artists/${artistId}`;
  }

  getArtistById(artistId: string): Promise<Artist> {
    return this.http.get(this.getArtistByIdURL(artistId))
      .mergeMap((response: any) => {
        const artist = mapToArtist(response);
        console.log(response);
        return Promise
          .all(response.release_ids
            .map((releaseID) => this.http.get(this.albumService.getAlbumByIdURL(releaseID))
              .toPromise())
          )
          .then((releases) => {
            console.log(releases);
            artist.albums = releases.map(mapToAlbum);
            return artist;
          });
      })
      .toPromise();
  }
}
