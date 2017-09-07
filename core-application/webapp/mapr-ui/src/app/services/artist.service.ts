import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Artist, Album} from "../models/artist";
import "rxjs/add/operator/map";
import "rxjs/add/operator/toPromise";
import "rxjs/add/operator/mergeMap";
import {AppConfig} from "../app.config";

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
    private config: AppConfig
  ) {
  }

  getArtistByIdURL(artistId: string): string {
    return `${this.config.apiURL}/api/1.0/artists/${artistId}`;
  }

  getArtistById(artistId: string): Promise<Artist> {
    return this.http.get(this.getArtistByIdURL(artistId))
      .map((response: any) => {
        console.log('Artist: ', response);
        const artist = mapToArtist(response);
        artist.albums = response.albums
          ? response.albums.map(mapToAlbum)
          : [];
        return artist;
      })
      .toPromise();
  }
}
