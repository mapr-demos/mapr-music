package com.mapr.music.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapr.music.annotation.MaprDbTable;

import java.util.List;

/**
 * Model class, which represents 'Album' document stored in MapR DB.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable("/apps/albums")
public class Album {

    @JsonProperty("_id")
    private String id;
    private String name;
    private String style;
    private String barcode;
    private String status;
    private String packaging;
    private String language;
    private String script;
    private String MBID;
    private String format;
    private String country;
    private List reviews;

    @JsonProperty("slug_name")
    private String slugName;

    @JsonProperty("slug_postfix")
    private Long slugPostfix;

    @JsonProperty("artist_list")
    private List artistList;

    @JsonProperty("catalog_numbers")
    private List catalogNumbers;

    @JsonProperty("track_list")
    private List<Track> trackList;

    @JsonProperty("cover_image_url")
    private String coverImageUrl;

    @JsonProperty("images_urls")
    private List<String> imagesUrls;

    @JsonProperty("released_date")
    private Long releasedDate;

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

    public Long getSlugPostfix() {
        return slugPostfix;
    }

    public Album setSlugPostfix(Long slugPostfix) {
        this.slugPostfix = slugPostfix;
        return this;
    }

    public List getArtistList() {
        return artistList;
    }

    public Album setArtistList(List artistList) {
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

    public Album setTrackList(List<Track> trackList) {
        this.trackList = trackList;
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

    public Long getReleasedDate() {
        return releasedDate;
    }

    public Album setReleasedDate(Long releasedDate) {
        this.releasedDate = releasedDate;
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

}
