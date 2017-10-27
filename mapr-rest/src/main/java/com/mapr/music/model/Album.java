package com.mapr.music.model;

import com.fasterxml.jackson.annotation.*;
import com.mapr.music.annotation.MaprDbTable;
import org.ojai.types.ODate;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mapr.music.util.MaprProperties.ALBUMS_TABLE_NAME;

/**
 * Model class, which represents 'Album' document stored in MapR DB.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable(ALBUMS_TABLE_NAME)
public class Album {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShortInfo {

        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("slug")
        private String slug;

        @JsonProperty("cover_image_url")
        private String coverImageUrl;

        @JsonProperty("rating")
        private Double rating;

        public static ShortInfo valueOf(Album album) {

            if (album == null) {
                throw new IllegalArgumentException("Album can not be null");
            }

            ShortInfo shortInfo = new ShortInfo();
            shortInfo.setId(album.getId());
            shortInfo.setName(album.getName());
            shortInfo.setCoverImageUrl(album.getCoverImageUrl());

            String slug = String.format("%s-%s", album.getSlugName(), album.getSlugPostfix());
            shortInfo.setSlug(slug);

            return shortInfo;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getCoverImageUrl() {
            return coverImageUrl;
        }

        public void setCoverImageUrl(String coverImageUrl) {
            this.coverImageUrl = coverImageUrl;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        @Override
        public String toString() {
            return "Album ShortInfo{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", slug='" + slug + '\'' +
                    ", coverImageUrl='" + coverImageUrl + '\'' +
                    '}';
        }
    }

    @JsonProperty("_id")
    private String id;

    @JsonProperty("slug_name")
    private String slugName;

    @JsonProperty("slug_postfix")
    private Long slugPostfix;

    @JsonProperty("artists")
    private List<Artist.ShortInfo> artists;

    @JsonProperty("catalog_numbers")
    private List catalogNumbers;

    @JsonProperty("tracks")
    private List<Track> trackList;

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("images_urls")
    private List<String> imagesUrls;

    private ODate releasedDate;

    @NotNull
    @JsonProperty("name")
    private String name;

    @JsonProperty("barcode")
    private String barcode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("packaging")
    private String packaging;

    @JsonProperty("language")
    private String language;

    @JsonProperty("script")
    private String script;

    @JsonProperty("MBID")
    private String MBID;

    @JsonProperty("format")
    private String format;

    @JsonProperty("country")
    private String country;

    @JsonProperty("reviews")
    private List reviews;

    @JsonProperty("rating")
    private Double rating;

    @JsonIgnore
    private ShortInfo shortInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlugName() {
        return slugName;
    }

    public void setSlugName(String slugName) {
        this.slugName = slugName;
    }

    public Long getSlugPostfix() {
        return slugPostfix;
    }

    public void setSlugPostfix(Long slugPostfix) {
        this.slugPostfix = slugPostfix;
    }

    public List<Artist.ShortInfo> getArtists() {
        return (artists != null) ? artists : Collections.emptyList();
    }

    public void setArtists(List<Artist.ShortInfo> artists) {
        this.artists = artists;
    }

    public void addArtist(Artist.ShortInfo artist) {

        if (this.artists == null) {
            this.artists = new ArrayList<>();
        }

        this.artists.add(artist);
    }

    public List getCatalogNumbers() {
        return catalogNumbers;
    }

    public void setCatalogNumbers(List catalogNumbers) {
        this.catalogNumbers = catalogNumbers;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    public List getReviews() {
        return reviews;
    }

    public void setReviews(List reviews) {
        this.reviews = reviews;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public List<String> getImagesUrls() {
        return imagesUrls;
    }

    public void setImagesUrls(List<String> imagesUrls) {
        this.imagesUrls = imagesUrls;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @JsonGetter("released_date")
    public ODate getReleasedDate() {
        return releasedDate;
    }

    public void setReleasedDate(ODate releasedDate) {
        this.releasedDate = releasedDate;
    }

    @JsonSetter("released_date")
    public void setReleasedDate(String dateDayString) {

        if (dateDayString == null) {
            this.releasedDate = null;
            return;
        }

        this.releasedDate = ODate.parse(dateDayString);
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getMBID() {
        return MBID;
    }

    public void setMBID(String MBID) {
        this.MBID = MBID;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public ShortInfo getShortInfo() {

        if (this.shortInfo == null) {
            this.shortInfo = ShortInfo.valueOf(this);
        }

        return shortInfo;
    }

    @Override
    public String toString() {
        return getShortInfo().toString();
    }
}
