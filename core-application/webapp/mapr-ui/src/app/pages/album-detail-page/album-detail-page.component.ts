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
  nameEditedTrack = '';
  reorderedTracks:Array<Track> = null;
  newTrack = null;

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
    const editedTrack = this.album.trackList.filter((track) => track.id === id)[0];
    if (this.editedTrackId === id) {
      editedTrack.name = this.nameEditedTrack;
      this.albumService.updateAlbumTrack(this.album.id, editedTrack).then(() => {
        this.editedTrackId = null;
        this.nameEditedTrack = '';
      });
      return;
    }
    this.editedTrackId = id;
    this.nameEditedTrack = editedTrack.name;
  }

  removeTrack(trackId: string) {
    this.albumService.deleteTrackInAlbum(this.album.id, trackId).then(() => {
      if (this.reorderedTracks) {
        this.reorderedTracks = this.reorderedTracks.filter((album) => album.id !== trackId);
      }
      this.album.trackList = this.album.trackList.filter((album) => album.id !== trackId);
    });
  }

  saveTracks() {
    this.albumService.saveAlbumTracks(this.album.id, this.reorderedTracks).then(() => {
      this.album.trackList = this.reorderedTracks;
      this.reorderedTracks = null;
    });
  }

  onAddNewTrackClick() {
    if (this.newTrack) {
      this.albumService.addTrackToAlbum(this.album.id, this.newTrack).then((track) => {
        if (this.reorderedTracks) {
          this.reorderedTracks.push(track);
        }
        this.album.trackList.push(track);
        this.newTrack = null;
      });
      return;
    }
    this.newTrack = {name: '', duration: '', position: 0};
  }
  onCancelAddClick() {
    this.newTrack = null;
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
