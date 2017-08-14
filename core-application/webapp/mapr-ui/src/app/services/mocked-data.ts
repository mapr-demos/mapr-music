import {Album} from '../models/album';
import {Artist} from '../models/artist';

const albums: Array<Album> = [];

const artists: Array<Artist> = [];

for (let i = 0; i< 17; i++) {
  artists.push({
    id: `Artist_${i}`,
    name: `Artist Name ${i}`,
    gender: 'male'
  });
}

const getArtistInfo = (album_index: number) => {
  const artist = artists[album_index % artists.length];
  return {
    id: artist.id,
    name: artist.name
  };
};

for (let i = 0; i < 400; i++) {
  albums.push({
    id: `Album_${i}`,
    title: `Test_${i}`,
    coverImageURL: 'https://img.discogs.com/f_1kbeUvSqknkebMjY-5qHd42T4=/300x300/filters:strip_icc():format(jpeg):mode_rgb():quality(40)/discogs-images/R-10684528-1502358314-3248.jpeg.jpg',
    artists: [
      getArtistInfo(i)
    ]
  });
}

export function getAlbums():Array<Album> {
  return albums;
}

export function getArtists():Array<Artist> {
  return artists;
}
