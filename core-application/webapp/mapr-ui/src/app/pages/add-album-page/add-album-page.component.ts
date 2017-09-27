import {Component, OnInit} from "@angular/core";
import {Album} from "../../models/album";
import {AlbumService} from "../../services/album.service";
import {Router} from "@angular/router";

const createNewAlbum = (): Album => ({
  id: '',
  title: '',
  coverImageURL: '',
  format: '',
  slug: '',
  trackList: [],
  country: '',
  artists: [],
  language: null,
  releasedDate: new Date()
});

@Component({
  selector: 'add-album-page',
  templateUrl: './add-album-page.component.html'
})
export class AddAlbumPage implements OnInit {
  constructor(private albumService: AlbumService,
              private router: Router) {
  }

  album: Album = null;
  errors: Array<string> = [];

  ngOnInit(): void {
    this.album = createNewAlbum();
  }

  onAlbumSave() {

    this.clearErrors();
    if (!this.albumValid()) {
      return;
    }
    console.log(this.album);

    this.albumService.createNewAlbum(this.album)
      .then((created) => {
        this.router.navigateByUrl(`album/${created.slug}`);
      });
  }

  albumValid(): boolean {

    if (!this.album.title) {
      this.errors.push("Title is required");
    }

    if (!this.album.language) {
      this.errors.push("Language is required");
    }

    if (!this.album.artists || this.album.artists.length == 0) {
      this.errors.push("Ar least one artist must be specified");
    }

    if (this.errors && this.errors.length > 0) {
      return false;
    }
   
    return true;
  }

  clearErrors() {
    this.errors = [];
  }
}
