import {Component, Input, OnInit} from "@angular/core";
import {Album, Track} from "../../models/album";
import {Subscription} from "rxjs";
import {AlbumService} from "../../services/album.service";
import uniqBy from 'lodash/uniqBy'
import uniqueId from 'lodash/uniqueId'

declare const $;

@Component({
  selector: 'album-edit-form',
  templateUrl: './album-edit-form.component.html',
})
export class AlbumEditForm implements OnInit{

  @Input()
  album: Album = null;
  newTrack: Track = null;

  artistsDisposable: Subscription = null;

  constructor(
    private albumService: AlbumService
  ) {
  }

  ngOnInit(): void {
    $('#new-artist').typeahead({
      source: this.getArtists.bind(this),
      fitToElement: true,
      showHintOnFocus: true,
      matcher: () => true,
      afterSelect: this.afterSelect.bind(this)
    });
  }

  getArtists(query, cb) {
    if (this.artistsDisposable !== null) {
      this.artistsDisposable.unsubscribe();
    }
    this.artistsDisposable = this.albumService.searchForArtists(query)
      .subscribe((artists) => {
        cb(artists)
        this.artistsDisposable = null;
      });
  }

  removeArtistById(artistId: string) {
    this.album.artists = this.album.artists.filter((artist) => artist.id !== artistId)
  }

  afterSelect(item) {
    $('#new-artist').typeahead('destroy');
    $('#new-artist')
      .val('')
      .text('')
      .change();
    $('#new-artist').typeahead({
      source: this.getArtists.bind(this),
      fitToElement: true,
      showHintOnFocus: true,
      matcher: () => true,
      afterSelect: this.afterSelect.bind(this)
    });
    const artists = this.album.artists.concat([item]);
    this.album.artists = uniqBy(artists, 'id');
  }

  onAddNewTrackClick() {
    if (this.newTrack) {
      this.newTrack.id = `${uniqueId()}`;
      //convert seconds to ms
      this.newTrack.duration = this.newTrack.duration + '000';
      this.album.trackList.push(this.newTrack);
      this.newTrack = null;
      return;
    }
    this.newTrack = {
      id: '',
      name: '',
      duration: '',
      position: ''
    };
  }
  removeTrack(trackId: string) {
    this.album.trackList = this.album.trackList.filter((track) => track.id !== trackId);
  }
}
