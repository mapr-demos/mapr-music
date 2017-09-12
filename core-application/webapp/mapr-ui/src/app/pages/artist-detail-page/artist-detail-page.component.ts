import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import 'rxjs/add/operator/switchMap';
import { ArtistService } from '../../services/artist.service';
import {Artist} from '../../models/artist';

@Component({
  selector: 'artist-page',
  templateUrl: './artist-detail-page.component.html',
  styleUrls: ['./artist-detail-page.component.css'],
})
export class ArtistPage implements OnInit {

  constructor(
    private router: ActivatedRoute,
    private artistService: ArtistService
  ) {}

  artist: Artist;
  sourceURL: string;

  ngOnInit(): void {
    this.router.paramMap
      .switchMap((params: ParamMap) => {
        // const artistId = params.get('artistId');
        // this.sourceURL = this.artistService.getArtistByIdURL(artistId);
        // return this.artistService.getArtistById(artistId);
        const artistSlug = params.get('artistSlug');
        this.sourceURL = this.artistService.getArtistBySlugURL(artistSlug);
        return this.artistService.getArtistBySlug(artistSlug);
      })
      .subscribe((artist) => {
        this.artist = artist;
      });
  }
}
