import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import 'rxjs/add/operator/switchMap';
import { ArtistService } from '../../services/artist.service';
import {Artist} from '../../models/artist';

@Component({
  selector: 'artist-page',
  templateUrl: './artist-page.component.html',
  styleUrls: ['./artist-page.component.css'],
})
export class ArtistPage implements OnInit {

  constructor(
    private router: ActivatedRoute,
    private artistService: ArtistService
  ) {}

  artist: Artist;

  ngOnInit(): void {
    this.router.paramMap
      .switchMap((params: ParamMap) => {
        return this.artistService.getById(params.get('artistId'));
      })
      .subscribe((artist) => {
        this.artist = artist;
      });
  }
}
