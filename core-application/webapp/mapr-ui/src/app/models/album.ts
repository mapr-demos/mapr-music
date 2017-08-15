export interface Artist {
  id: string,
  name: string
}

export interface Album {
  id: string,
  title: string,
  coverImageURL: string,
  country: string,
  artists: Array<Artist>
}

export interface AlbumsPage {
  albums: Array<Album>,
  totalNumber: number
}
