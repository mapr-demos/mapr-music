package com.mapr.music.dao;

import com.mapr.music.model.Recommendation;

import javax.inject.Named;

@Named("recommendationDao")
public class RecommendationDao extends MaprDbDao<Recommendation> {

    public RecommendationDao() {
        super(Recommendation.class);
    }

    /**
     * Updating recommendation documents is not supported.
     */
    @Override
    public Recommendation update(String id, Recommendation entity) {
        throw new UnsupportedOperationException("Recommendation updating is not supported");
    }
}
