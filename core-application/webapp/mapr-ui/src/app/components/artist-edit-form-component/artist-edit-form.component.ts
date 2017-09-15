import {Component, Input, OnInit} from "@angular/core";
import {Subscription} from "rxjs";
import uniqBy from "lodash/uniqBy";
import {Artist} from "../../models/artist";
import {ArtistService} from "../../services/artist.service";

declare const $;

@Component({
  selector: 'artist-edit-form',
  templateUrl: './artist-edit-form.component.html',
})
export class ArtistEditForm implements OnInit {

  @Input()
  artist: Artist = null;

  albumsDisposable: Subscription = null;

  constructor(private artistService: ArtistService) {
  }

  ngOnInit(): void {
    $('#new-album').typeahead({
      displayText: (item) => item.title,
      source: this.getAlbums.bind(this),
      fitToElement: true,
      showHintOnFocus: true,
      matcher: () => true,
      afterSelect: this.afterSelect.bind(this)
    });
  }

  getAlbums(query, cb) {
    if (this.albumsDisposable !== null) {
      this.albumsDisposable.unsubscribe();
    }
    this.albumsDisposable = this.artistService.searchForAlbums(query)
      .subscribe((albums) => {
        console.log(albums);
        cb(albums);
        this.albumsDisposable = null;
      });
  }

  removeAlbumsById(albumId: string) {
    this.artist.albums = this.artist.albums.filter((album) => album.id !== albumId)
  }

  afterSelect(item) {
    $('#new-album').typeahead('destroy');
    $('#new-album')
      .val('')
      .text('')
      .change();
    $('#new-album').typeahead({
      source: this.getAlbums.bind(this),
      fitToElement: true,
      showHintOnFocus: true,
      matcher: () => true,
      afterSelect: this.afterSelect.bind(this)
    });
    const albums = this.artist.albums.concat([item]);
    this.artist.albums = uniqBy(albums, 'id');
  }

}
