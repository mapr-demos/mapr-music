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
@MaprDbTable("/apps/artists")
public class Artist {

    @JsonProperty("_id")
    private String id;
    private String name;
    private String gender;
    private String area;

    @JsonProperty("slug_name")
    private String slugName;

    @JsonProperty("slug_postfix")
    private Long slugPostfix;

    @JsonProperty("IPI")
    private String ipi;

    @JsonProperty("ISNI")
    private String isni;

    @JsonProperty("MBID")
    private String mbid;

    @JsonProperty("disambiguation_comment")
    private String disambiguationComment;

    @JsonProperty("albums")
    private List<String> albumsIds;

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

    public Artist setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Artist setName(String name) {
        this.name = name;
        return this;
    }

    public String getSlugName() {
        return slugName;
    }

    public Artist setSlugName(String slugName) {
        this.slugName = slugName;
        return this;
    }

    public Long getSlugPostfix() {
        return slugPostfix;
    }

    public Artist setSlugPostfix(Long slugPostfix) {
        this.slugPostfix = slugPostfix;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public Artist setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getArea() {
        return area;
    }

    public Artist setArea(String area) {
        this.area = area;
        return this;
    }

    public String getIpi() {
        return ipi;
    }

    public Artist setIpi(String ipi) {
        this.ipi = ipi;
        return this;
    }

    public String getIsni() {
        return isni;
    }

    public Artist setIsni(String isni) {
        this.isni = isni;
        return this;
    }

    public String getMbid() {
        return mbid;
    }

    public Artist setMbid(String mbid) {
        this.mbid = mbid;
        return this;
    }

    public String getDisambiguationComment() {
        return disambiguationComment;
    }

    public Artist setDisambiguationComment(String disambiguationComment) {
        this.disambiguationComment = disambiguationComment;
        return this;
    }

    public List<String> getAlbumsIds() {
        return albumsIds;
    }

    public Artist setAlbumsIds(List<String> albumsIds) {
        this.albumsIds = albumsIds;
        return this;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Artist setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public String[] getImagesUrls() {
        return imagesUrls;
    }

    public Artist setImagesUrls(String[] imagesUrls) {
        this.imagesUrls = imagesUrls;
        return this;
    }

    public Long getBeginDate() {
        return beginDate;
    }

    public Artist setBeginDate(Long beginDate) {
        this.beginDate = beginDate;
        return this;
    }

    public Long getEndDate() {
        return endDate;
    }

    public Artist setEndDate(Long endDate) {
        this.endDate = endDate;
        return this;
    }
}
