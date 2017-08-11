export class Album {
  constructor(
    public id: string,
    public name: string,
    public imageURL: string,
  ){}
}

export interface AlbumsPage {
  albums: Array<Album>,
  totalNumber: number
}
