package com.mapr.music.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapr.music.annotation.MaprDbTable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable("/apps/artist")
public class Artist {

    @JsonProperty("_id")
    private String id;
    private String name;
    private String gender;
    private String area;
    private String ipi;
    private String isni;
    private String mbid;

    @JsonProperty("disambiguation_comment")
    private String disambiguationComment;

    @JsonProperty("release_ids")
    private String[] releaseIds;

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

    public String[] getReleaseIds() {
        return releaseIds;
    }

    public Artist setReleaseIds(String[] releaseIds) {
        this.releaseIds = releaseIds;
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
