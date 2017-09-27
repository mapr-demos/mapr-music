import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {ArtistService} from "../../services/artist.service";
import {Artist} from "../../models/artist";

@Component({
  selector: 'edit-artist-page',
  templateUrl: './edit-artist-page.component.html'
})
export class EditArtistPage implements OnInit {
  constructor(private artistService: ArtistService,
              private activatedRoute: ActivatedRoute,
              private router: Router) {
  }

  artist: Artist = null;
  errors: Array<string> = [];

  ngOnInit(): void {
    this.activatedRoute.paramMap
      .switchMap((params: ParamMap) => {
        const artistSlug = params.get('artistSlug');
        return this.artistService.getArtistBySlug(artistSlug);
      })
      .subscribe((artist) => {
        this.artist = artist;
      });
  }

  onArtistSave() {

    this.clearErrors();
    if (!this.artistValid()) {
      return;
    }

    this.artistService.updateArtist(this.artist)
      .then((artist) => {
        this.router.navigateByUrl(`artist/${artist.slug}`);
      });
  }

  artistValid(): boolean {

    if (!this.artist.name) {
      this.errors.push("Name is required");
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
