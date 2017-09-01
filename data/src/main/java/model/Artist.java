package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artist {

    @JsonIgnore
    private String pk;
    @JsonIgnore
    private String artistCreditId;

    @JsonIgnore
    private Set<ArtistUrlLink> links = new HashSet<>();

    @JsonProperty("_id")
    private String id;
    private String name;

    @JsonProperty("slug_name")
    private String slugName;

    @JsonProperty("slug_postfix")
    private long slugPostfix;

    private String gender;
    private String area;

    @JsonProperty("IPI")
    private String ipi;

    @JsonProperty("ISNI")
    private String isni;

    @JsonProperty("MBID")
    private String MBID;

    @JsonProperty("disambiguation_comment")
    private String disambiguationComment;

    @JsonProperty("albums")
    private List<String> albumsIds = new LinkedList<>();

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("images_urls")
    private Set<String> imagesUrls = new HashSet<>();

    @JsonProperty("begin_date")
    private Long beginDate;

    @JsonProperty("end_date")
    private Long endDate;

    public Set<ArtistUrlLink> getLinks() {
        return links;
    }

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

    public long getSlugPostfix() {
        return slugPostfix;
    }

    public Artist setSlugPostfix(long slugPostfix) {
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

    public String getMBID() {
        return MBID;
    }

    public Artist setMBID(String MBID) {
        this.MBID = MBID;
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

    public Set<String> getImagesUrls() {
        return imagesUrls;
    }

    public Artist setImagesUrls(Set<String> imagesUrls) {
        this.imagesUrls = imagesUrls;
        return this;
    }

    public Artist addImageUrl(String imageUrl) {
        imagesUrls.add(imageUrl);
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

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getArtistCreditId() {
        return artistCreditId;
    }

    public Artist setArtistCreditId(String artistCreditId) {
        this.artistCreditId = artistCreditId;
        return this;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this)
                .append("PK", pk)
                .append("artistCreditId", artistCreditId)
                .append("id", id)
                .append("name", name)
                .append("gender", gender)
                .append("area", area)
                .append("ipi", ipi)
                .append("isni", isni)
                .append("MBID", MBID)
                .append("disambiguationComment", disambiguationComment)
                .append("albumsIds", albumsIds)
                .append("profileImageUrl", profileImageUrl)
                .append("imagesUrls", imagesUrls)
                .append("beginDate", beginDate)
                .append("endDate", endDate)
                .build();

    }
}