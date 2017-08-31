package model;

public class ArtistUrlLink {

    private String linkId;
    private String urlId;
    private String linkTypeId;

    public ArtistUrlLink() {

    }

    public ArtistUrlLink(String linkId, String urlId) {
        this.linkId = linkId;
        this.urlId = urlId;
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
}
