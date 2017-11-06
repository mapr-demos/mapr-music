export interface Album {
  id: string,
  coverImageURL: string,
  slug: string,
  title: string
}

export interface Artist {
  id: string,
  name: string,
  avatarURL: string,
  gender: string,
  slug: string,
  area: string,
  disambiguationComment: string,
  beginDate: string,
  endDate: string,
  rating: number,
  IPI,
  ISNI
  albums: Array<Album>
}

export interface ArtistsPage {
  artists: Array<Artist>,
  totalNumber: number
}
