import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, ParamMap} from "@angular/router";
import "rxjs/add/operator/switchMap";
import {Album, Track} from "../../models/album";
import {AlbumService} from "../../services/album.service";

function swap(arr, a: number, b: number): void {
  const t = arr[a];
  arr[a] = arr[b];
  arr[b] = t;
}

function removeAtInd(arr, ind) {
  return arr.slice(0, ind).concat(arr.splice(ind + 1))
}

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

  editedTrackId = null;
  reorderedTracks:Array<Track> = null;

  isFirstTrack(ind: number):boolean {
    return ind === 0;
  }

  isLastTrack(ind: number):boolean {
    return this.reorderedTracks.length === ind + 1;
  }

  toggleReorderMode() {
    if (this.reorderedTracks) {
      this.reorderedTracks = null;
      return;
    }
    this.reorderedTracks = this.album.trackList.slice(0,  this.album.trackList.length);
  }

  moveTrackUp(ind:number) {
    swap(this.reorderedTracks, ind, ind - 1);
  }

  moveTrackDown(ind:number) {
    swap(this.reorderedTracks, ind, ind + 1);
  }

  setEditTrackId(id: string) {
    if (this.editedTrackId === id) {
      this.editedTrackId = null;
      return;
    }
    this.editedTrackId = id;
  }

  removeTrackAtIndex(ind: number) {
    if (this.reorderedTracks) {
      this.reorderedTracks = removeAtInd(this.reorderedTracks, ind);
    }
    const {trackList} = this.album;
    this.album.trackList = removeAtInd(trackList, ind);
  }

  ngOnInit(): void {
    this.router.paramMap
      .switchMap((params: ParamMap) => {
        const albumSlug = params.get('albumSlug');
        this.sourceURL = this.albumService.getAlbumBySlugURL(albumSlug);
        return this.albumService.getAlbumBySlug(albumSlug);
      })
      .subscribe((album) => {
        this.album = album;
      });
  }

}
