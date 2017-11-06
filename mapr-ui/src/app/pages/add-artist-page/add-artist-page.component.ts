import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {Artist} from "../../models/artist";
import {ArtistService} from "../../services/artist.service";

const createNewArtist = (): Artist => ({
  id: '',
  name: '',
  avatarURL: '',
  gender: '',
  area: '',
  slug: '',
  beginDate: '',
  endDate: '',
  albums: [],
  disambiguationComment: '',
  IPI: '',
  rating: 0,
  ISNI: ''
});

@Component({
  selector: 'add-artist-page',
  templateUrl: './add-artist-page.component.html'
})
export class AddArtistPage implements OnInit {
  constructor(private artistService: ArtistService,
              private router: Router) {
  }

  artist: Artist = null;
  errors: Array<string> = [];

  ngOnInit(): void {
    this.artist = createNewArtist();
  }

  onArtistSave() {

    this.clearErrors();
    if (!this.artistValid()) {
      return;
    }

    console.log(this.artist);
    this.artistService.createNewArtist(this.artist)
      .then((created) => {
        this.router.navigateByUrl(`artist/${created.slug}`);
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
