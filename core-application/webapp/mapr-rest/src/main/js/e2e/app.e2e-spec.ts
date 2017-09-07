import {MaprUiPage} from "./app.po";

describe('mapr-ui App', () => {
  let page: MaprUiPage;

  beforeEach(() => {
    page = new MaprUiPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!');
  });
});
