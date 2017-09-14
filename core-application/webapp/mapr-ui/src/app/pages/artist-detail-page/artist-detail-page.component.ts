import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import "rxjs/add/operator/switchMap";
import {ArtistService} from "../../services/artist.service";
import {Artist} from "../../models/artist";
import {ReplaySubject} from "rxjs/ReplaySubject";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'artist-page',
  templateUrl: './artist-detail-page.component.html',
  styleUrls: ['./artist-detail-page.component.css'],
})
export class ArtistPage implements OnInit {

  isAuthenticated: ReplaySubject<boolean>;

  constructor(private authService: AuthService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private artistService: ArtistService) {
    this.isAuthenticated = this.authService.isAuthenticated$;
  }

  artist: Artist;
  sourceURL: string;

  ngOnInit(): void {
    this.activatedRoute.paramMap
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

  deleteArtist() {
    this.artistService.deleteArtist(this.artist)
      .then(() => {
        this.router.navigateByUrl('');
      });
  }
}
