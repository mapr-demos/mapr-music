import {Component, OnInit} from "@angular/core";
import {Album} from "../../models/album";
import {AlbumService} from "../../services/album.service";
import {Router} from "@angular/router";

const createNewAlbum = (): Album => ({
  id: '',
  title: '',
  coverImageURL: '',
  format: '',
  style: '',
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
export class AddAlbumPage implements OnInit{
  constructor(
    private albumService: AlbumService,
    private router: Router
  ) {}

  album: Album = null;

  ngOnInit(): void {
    this.album = createNewAlbum();
  }

  onAlbumSave() {
    console.log(this.album);
    this.albumService.createNewAlbum(this.album)
      .then((created) => {
        this.router.navigateByUrl(`album/${created.slug}`);
      });
  }
}
