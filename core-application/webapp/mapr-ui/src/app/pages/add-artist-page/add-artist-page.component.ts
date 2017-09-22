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

  ngOnInit(): void {
    this.artist = createNewArtist();
  }

  onArtistSave() {
    console.log(this.artist);
    this.artistService.createNewArtist(this.artist)
      .then((created) => {
        this.router.navigateByUrl(`artist/${created.slug}`);
      });
  }
}
