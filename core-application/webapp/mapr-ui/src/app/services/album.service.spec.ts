import {AlbumService} from "./album.service";
import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";
import {Observable} from "rxjs";
import {AlbumsPage, Album} from "../models/album";
import createSpy = jasmine.createSpy;

function mockGet(client, response) {
  client.get = createSpy('get').and.returnValue(Observable.of(response));
}

describe('Service: Album Service', () => {
  let service: AlbumService;
  let client: HttpClient;
  const config = new AppConfig();
  beforeEach(() => {
    client = {} as HttpClient;

    service = new AlbumService(
      client,
      config
    );
  });
  it('getAlbumsPage', (done) => {
    const response = {
      results: [
        {
          _id: 'test_id',
          name: 'Test_Name',
          cover_image_url: 'Test_image',
          country: 'Test Country',
          style: 'Some Style',
          format: 'Test_format',
          genre: 'test_genre'
        }
      ],
      pagination: {
        items: 34
      }
    };
    mockGet(client, response);
    const getAlbumsPageURLSpy = spyOn(service, 'getAlbumsPageURL');
    service.getAlbumsPage({pageNumber: 2, sortType: 'RELEASE_DESC'})
      .then((albumsPage: AlbumsPage) => {
        expect(getAlbumsPageURLSpy).toHaveBeenCalled();
        expect(albumsPage).toBeDefined();
        expect(albumsPage.totalNumber).toBe(34);
        const firstAlbum = albumsPage.albums[0];
        expect(firstAlbum.id).toBe('test_id');
        expect(firstAlbum.artists).toEqual([]);
        expect(firstAlbum.trackList).toEqual([]);
        expect(firstAlbum.title).toBe('Test_Name');
        done();
      })
      .catch((err) => {
        fail(err);
        done();
      });
  });
  it('getAlbumBySlug', (done) => {
    const response = {
      _id: 'test_id',
      name: 'Test_Name',
      cover_image_url: 'Test_image',
      country: 'Test Country',
      style: 'Some Style',
      format: 'Test_format',
      genre: 'test_genre',
      track_list: [
        {
          id: '',
          name: 'Test_Track_name',
          length: '10000'
        }
      ],
      artist_list: [
        {
          artist_id: 'Test_artist_id',
          artist_name: 'Test_Artist_name'
        }
      ]
    };
    mockGet(client, response);
    const getAlbumBySlugURLSpy = spyOn(service, 'getAlbumBySlugURL');
    service.getAlbumBySlug('')
      .then((album: Album) => {
        expect(getAlbumBySlugURLSpy).toHaveBeenCalled();
        expect(album.id).toBe('test_id');
        expect(album.artists).toEqual([
          {
            id:'Test_artist_id',
            name: 'Test_Artist_name'
          }
        ]);
        expect(album.trackList).toEqual([{
          id: '',
          name: 'Test_Track_name',
          duration: '10000'
        }]);
        expect(album.title).toBe('Test_Name');
        done();
      })
      .catch((err) => {
        fail(err);
        done();
      });
  });
});
