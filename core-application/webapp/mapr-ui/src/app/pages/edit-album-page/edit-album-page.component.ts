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
    this.albumService.updateAlbum(this.album)
      .then((album) => {
        this.router.navigateByUrl(`album/${album.slug}`);
      });
  }
}
