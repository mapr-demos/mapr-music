import {Component, Input, OnInit} from "@angular/core";
import {Album, Track, Language} from "../../models/album";
import {Subscription} from "rxjs";
import {AlbumService} from "../../services/album.service";
import uniqBy from 'lodash/uniqBy';
import uniqueId from 'lodash/uniqueId';
import find from 'lodash/find';
import {LanguageService} from "../../services/language.service";

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
  languageOptions: Array<Language>;

  constructor(
    private albumService: AlbumService,
    private languageService: LanguageService
  ) {
  }

  ngOnInit(): void {
    this.languageService.getAllLanguages()
      .then((languages) => {
        this.languageOptions = languages;
      });
    console.log(this.album.releasedDate);
    $('#date-selector')
      .datetimepicker({
        format: 'MM/DD/YYYY',
        maxDate: Date.now(),
        defaultDate: this.album.releasedDate,
        useCurrent: false
      })
      .on('dp.hide', (event) => {
        console.log(event.date.toDate().getTime());
        this.album.releasedDate = event.date.toDate();
      });
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
  onLanguageChange(languageCode: string) {
    this.album.language = languageCode === 'null'
      ? null
      : find(this.languageOptions, (language) => language.code === languageCode);
  }
}
