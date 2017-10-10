package com.mapr.music.dao.impl;

import com.mapr.music.model.Recommendation;

import javax.inject.Named;

/**
 * @author Vlad Glinskiy
 */
@Named("recommendationDao")
public class RecommendationDaoImpl extends MaprDbDaoImpl<Recommendation> {

    public RecommendationDaoImpl() {
        super(Recommendation.class);
    }

    @Override
    public Recommendation update(String id, Recommendation entity) {
        throw new UnsupportedOperationException();
    }
}
