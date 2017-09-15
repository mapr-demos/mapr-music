package com.mapr.music.model;

import com.fasterxml.jackson.annotation.*;
import com.mapr.music.annotation.MaprDbTable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class, which represents 'Album' document stored in MapR DB.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable("/apps/artists")
public class Artist {

    private String id;

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

    @NotNull
    private String name;
    private String gender;
    private String area;

    @JsonGetter("_id")
    public String getId() {
        return id;
    }

    @JsonSetter("_id")
    public Artist setId(String id) {
        this.id = id;
        return this;
    }

    @JsonSetter("id")
    public Artist setIdWithoutUnderscore(String id) {
        this.id = id;
        return this;
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

    public Artist setDisambiguationComment(String disambiguationComment) {
        this.disambiguationComment = disambiguationComment;
        return this;
    }

    public List<String> getAlbumsIds() {
        return albumsIds;
    }

    public void setAlbumsIds(List<String> albumsIds) {
        this.albumsIds = albumsIds;
    }

    public void addAlbumId(String albumsId) {

        if (this.albumsIds == null) {
            this.albumsIds = new ArrayList<>();
        }
        this.albumsIds.add(albumsId);
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
