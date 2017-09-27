import {Component, OnInit} from "@angular/core";
import {Album} from "../../models/album";
import {AlbumService} from "../../services/album.service";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";

@Component({
  selector: 'edit-album-page',
  templateUrl: './edit-album-page.component.html'
})
export class EditAlbumPage implements OnInit{
  constructor(
    private albumService: AlbumService,
    private activatedRoute: ActivatedRoute,
    private router: Router
  ) {}

  album: Album = null;
  errors: Array<string> = [];

  ngOnInit(): void {
    this.activatedRoute.paramMap
      .switchMap((params: ParamMap) => {
        const albumSlug = params.get('albumSlug');
        return this.albumService.getAlbumBySlug(albumSlug);
      })
      .subscribe((album) => {
        this.album = album;
      });
  }

  onAlbumSave() {

    this.clearErrors();
    if (!this.albumValid()) {
      return;
    }

    this.albumService.updateAlbum(this.album)
      .then((album) => {
        this.router.navigateByUrl(`album/${album.slug}`);
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
