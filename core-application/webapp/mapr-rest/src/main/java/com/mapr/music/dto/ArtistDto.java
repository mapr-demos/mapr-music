package com.mapr.music.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Data Transfer Object for {@link com.mapr.music.model.Artist} model class.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistDto {

    @JsonProperty("_id")
    private String id;
    private String name;
    private String gender;
    private String area;

    @JsonProperty("IPI")
    private String ipi;

    @JsonProperty("ISNI")
    private String isni;

    @JsonProperty("MBID")
    private String mbid;

    @JsonProperty("disambiguation_comment")
    private String disambiguationComment;

    @JsonProperty("albums")
    private List<AlbumDto> albums;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("images_urls")
    private String[] imagesUrls;

    @JsonProperty("begin_date")
    private Long beginDate;

    @JsonProperty("end_date")
    private Long endDate;

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getIpi() {
        return ipi;
    }

    public void setIpi(String ipi) {
        this.ipi = ipi;
    }

    public String getIsni() {
        return isni;
    }

    public void setIsni(String isni) {
        this.isni = isni;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public String getDisambiguationComment() {
        return disambiguationComment;
    }

    public void setDisambiguationComment(String disambiguationComment) {
        this.disambiguationComment = disambiguationComment;
    }

    public List<AlbumDto> getAlbums() {
        return albums;
    }

    public void setAlbums(List<AlbumDto> albums) {
        this.albums = albums;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String[] getImagesUrls() {
        return imagesUrls;
    }

    public void setImagesUrls(String[] imagesUrls) {
        this.imagesUrls = imagesUrls;
    }

    public Long getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Long beginDate) {
        this.beginDate = beginDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
}
