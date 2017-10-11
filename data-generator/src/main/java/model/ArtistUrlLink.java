package model;

public class ArtistUrlLink {

    private String linkId;
    private String urlId;
    private String linkTypeId;
    private String artistPk;

    public ArtistUrlLink() {

    }

    public ArtistUrlLink(String linkId, String urlId) {
        this(linkId, urlId, null);
    }

    public ArtistUrlLink(String linkId, String urlId, String linkTypeId) {
        this.linkId = linkId;
        this.urlId = urlId;
        this.linkTypeId = linkTypeId;
    }

    public ArtistUrlLink(String linkId, String urlId, String linkTypeId, String artistPk) {
        this.linkId = linkId;
        this.urlId = urlId;
        this.linkTypeId = linkTypeId;
        this.artistPk = artistPk;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getUrlId() {
        return urlId;
    }

    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

    public String getLinkTypeId() {
        return linkTypeId;
    }

    public void setLinkTypeId(String linkTypeId) {
        this.linkTypeId = linkTypeId;
    }

    public String getArtistPk() {
        return artistPk;
    }

    public void setArtistPk(String artistPk) {
        this.artistPk = artistPk;
    }
}
