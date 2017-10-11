export interface SearchResult {
  id: string,
  type: string,
  name: string,
  imageURL: string,
  slug: string
}

export interface SearchResultsPage {
  searchResults : Array<SearchResult>,
  totalNumber: number
}
