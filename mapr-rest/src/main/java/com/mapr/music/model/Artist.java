package com.mapr.music.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapr.music.annotation.MaprDbTable;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mapr.music.util.MaprProperties.ARTISTS_TABLE_NAME;

/**
 * Model class, which represents 'Album' document stored in MapR DB.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable(ARTISTS_TABLE_NAME)
public class Artist {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ShortInfo {

        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("slug")
        private String slug;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        @JsonProperty("rating")
        private Double rating;

        public static ShortInfo valueOf(Artist artist) {

            if (artist == null) {
                throw new IllegalArgumentException("Artist can not be null");
            }

            ShortInfo shortInfo = new ShortInfo();
            shortInfo.setId(artist.getId());
            shortInfo.setName(artist.getName());
            shortInfo.setProfileImageUrl(artist.getProfileImageUrl());

            String slug = String.format("%s-%s", artist.getSlugName(), artist.getSlugPostfix());
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

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        @Override
        public String toString() {
            return "Artist ShortInfo{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", slug='" + slug + '\'' +
                    ", profileImageUrl='" + profileImageUrl + '\'' +
                    '}';
        }
    }

    @JsonProperty("_id")
    private String id;

    @NotNull
    @JsonProperty("name")
    private String name;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("area")
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
    private List<Album.ShortInfo> albums;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("images_urls")
    private String[] imagesUrls;

    @JsonProperty("begin_date")
    private Long beginDate;

    @JsonProperty("end_date")
    private Long endDate;

    @JsonProperty("deleted")
    private Boolean deleted;

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

    public List<Album.ShortInfo> getAlbums() {
        return (albums != null) ? albums : Collections.emptyList();
    }

    public void setAlbums(List<Album.ShortInfo> albums) {
        this.albums = albums;
    }

    public void addAlbum(Album.ShortInfo albumShortInfo) {

        if (this.albums == null) {
            this.albums = new ArrayList<>();
        }
        this.albums.add(albumShortInfo);
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

    public ShortInfo getShortInfo() {

        if (this.shortInfo == null) {
            this.shortInfo = ShortInfo.valueOf(this);
        }

        return this.shortInfo;
    }

    @Override
    public String toString() {
        return getShortInfo().toString();
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
