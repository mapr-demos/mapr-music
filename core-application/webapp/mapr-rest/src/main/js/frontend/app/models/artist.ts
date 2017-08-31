export interface Album {
  id: string,
  coverImageURL: string,
  title: string
}

export interface Artist {
  id: string,
  name: string,
  avatarURL: string,
  gender: string,
  albums: Array<Album>
}
