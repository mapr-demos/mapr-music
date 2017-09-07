import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";
import {Observable} from "rxjs";
import {ArtistService} from "./artist.service";
import {Artist} from "../models/artist";
import createSpy = jasmine.createSpy;

function mockGet(client, response) {
  client.get = createSpy('get').and.returnValue(Observable.of(response));
}

describe('Service: Artist Service', () => {
  let service: ArtistService;
  let client: HttpClient;
  const config = new AppConfig();
  beforeEach(() => {
    client = {} as HttpClient;

    service = new ArtistService(
      client,
      config
    );
  });
  it('getArtistById', (done) => {
    const response = {
      _id: 'Test_artist_id',
      name: 'Test_name',
      profile_image_url: 'testProfileImage',
      gender: 'Test_gender',
      albums: [
        {
          _id: 'TEST_album_Id',
          name: 'Test_album_name',
          cover_image_url: 'Test_album_cover'
        }
      ]
    };
    mockGet(client, response);
    const getArtistByIdURLSpy = spyOn(service, 'getArtistByIdURL');
    service.getArtistById('')
      .then((artist: Artist) => {
        expect(getArtistByIdURLSpy).toHaveBeenCalled();
        expect(artist).toBeDefined();
        expect(artist.id).toBe('Test_artist_id');
        expect(artist.name).toBe('Test_name');
        expect(artist.gender).toBe('Test_gender');
        expect(artist.avatarURL).toBe('testProfileImage');
        expect(artist.albums).toEqual([{
          id: 'TEST_album_Id',
          title: 'Test_album_name',
          coverImageURL: 'Test_album_cover'
        }]);
        done();
      })
      .catch((err) => {
        fail(err);
        done();
      });
  });
});
