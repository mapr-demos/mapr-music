package com.mapr.music.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapr.music.annotation.MaprDbTable;

import java.util.List;

/**
 * Model class, which represents 'Recommendation' document stored in MapR DB.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable("/apps/recommendations")
public class Recommendation {

    @JsonProperty("_id")
    private String id;

    @JsonProperty("recommended_albums")
    private List<String> recommendedAlbumsIds;

    @JsonProperty("recommended_artists")
    private List<String> recommendedArtistsIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getRecommendedAlbumsIds() {
        return recommendedAlbumsIds;
    }

    public void setRecommendedAlbumsIds(List<String> recommendedAlbumsIds) {
        this.recommendedAlbumsIds = recommendedAlbumsIds;
    }

    public List<String> getRecommendedArtistsIds() {
        return recommendedArtistsIds;
    }

    public void setRecommendedArtistsIds(List<String> recommendedArtistsIds) {
        this.recommendedArtistsIds = recommendedArtistsIds;
    }
}
