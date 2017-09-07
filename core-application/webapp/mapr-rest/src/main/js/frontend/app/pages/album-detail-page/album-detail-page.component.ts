import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, ParamMap} from "@angular/router";
import "rxjs/add/operator/switchMap";
import {Album} from "../../models/album";
import {AlbumService} from "../../services/album.service";

@Component({
  selector: 'album-detail-page',
  templateUrl: './album-detail-page.component.html',
  styleUrls: ['./album-detail-page.component.css']
})
export class AlbumDetailPage implements OnInit{
  constructor(
    private router: ActivatedRoute,
    private albumService: AlbumService
  ) {}

  album: Album;
  sourceURL: string;

  ngOnInit(): void {
    this.router.paramMap
      .switchMap((params: ParamMap) => {
        const albumId = params.get('albumId');
        this.sourceURL = this.albumService.getAlbumByIdURL(albumId);
        return this.albumService.getAlbumById(albumId);
      })
      .subscribe((album) => {
        this.album = album;
      });
  }

}
