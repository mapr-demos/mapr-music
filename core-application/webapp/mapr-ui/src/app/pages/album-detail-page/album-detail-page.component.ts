import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import 'rxjs/add/operator/switchMap';
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

  ngOnInit(): void {
    this.router.paramMap
      .switchMap((params: ParamMap) => {
        return this.albumService.getById(params.get('albumId'))
      })
      .subscribe((album) => {
        this.album = album;
      });
  }

}
