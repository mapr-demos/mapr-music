package model;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Album {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class ShortInfo {

        @JsonGetter("id")
        public String getId() {
            return Album.this.getId();
        }

        @JsonGetter("name")
        public String getName() {
            return Album.this.getName();
        }

        @JsonGetter("slug")
        public String getSlug() {
            return String.format("%s-%s", Album.this.getSlugName(), Album.this.getSlugPostfix().get$numberLong());
        }

        @JsonGetter("cover_image_url")
        public String getCoverImageUrl() {
            return Album.this.getCoverImageUrl();
        }

        @JsonGetter("rating")
        public Double getRating() {
            return Album.this.getRating();
        }
    }

    @JsonIgnore
    private String pk;

    @JsonIgnore
    private String mediumId;

    @JsonIgnore
    private String releaseGroupId;

    @JsonProperty("_id")
    private String id;
    private String name;

    @JsonProperty("slug_name")
    private String slugName;

    @JsonProperty("slug_postfix")
    private JsonNumberLong slugPostfix;

    private String style;
    private String barcode;
    private String status;
    private String packaging;
    private String language;
    private String script;

    @JsonProperty("MBID")
    private String MBID;

    private String format;
    private String country;
    private List reviews;

    @JsonProperty("artists")
    private List<Artist.ShortInfo> artistList = new ArrayList<>();

    @JsonProperty("catalog_numbers")
    private List catalogNumbers;

    @JsonProperty("tracks")
    private List<Track> trackList = new ArrayList<>();

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("images_urls")
    private List<String> imagesUrls;

    @JsonProperty("released_date")
    private JsonNumberLong releasedDate;

    @JsonProperty("rating")
    private Double rating;

    @JsonIgnore
    private ShortInfo shortInfo;

    public String getReleaseGroupId() {
        return releaseGroupId;
    }

    public void setReleaseGroupId(String releaseGroupId) {
        this.releaseGroupId = releaseGroupId;
    }

    public String getId() {
        return id;
    }

    public Album setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Album setName(String name) {
        this.name = name;
        return this;
    }

    public String getSlugName() {
        return slugName;
    }

    public Album setSlugName(String slugName) {
        this.slugName = slugName;
        return this;
    }

    public JsonNumberLong getSlugPostfix() {
        return slugPostfix;
    }

    public Album setSlugPostfix(long slugPostfix) {
        this.slugPostfix = new JsonNumberLong(slugPostfix);
        return this;
    }

    public List<Artist.ShortInfo> getArtistList() {
        return artistList;
    }

    public Album setArtistList(List<Artist.ShortInfo> artistList) {
        this.artistList = artistList;
        return this;
    }

    public List getCatalogNumbers() {
        return catalogNumbers;
    }

    public Album setCatalogNumbers(List catalogNumbers) {
        this.catalogNumbers = catalogNumbers;
        return this;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public Album setTrackList(List trackList) {
        this.trackList = trackList;
        return this;
    }

    public Album addTrack(Track track) {
        this.trackList.add(track);
        return this;
    }

    public List getReviews() {
        return reviews;
    }

    public Album setReviews(List reviews) {
        this.reviews = reviews;
        return this;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public Album setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
        return this;
    }

    public List<String> getImagesUrls() {
        return imagesUrls;
    }

    public Album setImagesUrls(List<String> imagesUrls) {
        this.imagesUrls = imagesUrls;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public Album setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public Album setCountry(String country) {
        this.country = country;
        return this;
    }

    public JsonNumberLong getReleasedDate() {
        return releasedDate;
    }

    public Album setReleasedDate(Long releasedDate) {
        this.releasedDate = (releasedDate != null) ? new JsonNumberLong(releasedDate) : null;
        return this;
    }

    public String getStyle() {
        return style;
    }

    public Album setStyle(String style) {
        this.style = style;
        return this;
    }

    public String getBarcode() {
        return barcode;
    }

    public Album setBarcode(String barcode) {
        this.barcode = barcode;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Album setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getPackaging() {
        return packaging;
    }

    public Album setPackaging(String packaging) {
        this.packaging = packaging;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public Album setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getScript() {
        return script;
    }

    public Album setScript(String script) {
        this.script = script;
        return this;
    }

    public String getMBID() {
        return MBID;
    }

    public Album setMBID(String MBID) {
        this.MBID = MBID;
        return this;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getMediumId() {
        return mediumId;
    }

    public void setMediumId(String mediumId) {
        this.mediumId = mediumId;
    }

    public ShortInfo getShortInfo() {

        if (shortInfo == null) {
            shortInfo = new ShortInfo();
        }

        return shortInfo;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("PK", pk)
                .append("mediumId", mediumId)
                .append("id", id)
                .append("name", name)
                .append("style", style)
                .append("barcode", barcode)
                .append("status", status)
                .append("packaging", packaging)
                .append("language", language)
                .append("script", script)
                .append("MBID", MBID)
                .append("format", format)
                .append("country", country)
                .append("reviews", reviews)
                .append("artistList", artistList)
                .append("catalogNumbers", catalogNumbers)
                .append("trackList", trackList)
                .append("coverImageUrl", coverImageUrl)
                .append("imagesUrls", imagesUrls)
                .append("releasedDate", releasedDate)
                .toString();

    }
}
