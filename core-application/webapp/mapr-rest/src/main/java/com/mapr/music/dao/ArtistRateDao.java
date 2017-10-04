package com.mapr.music.dao;

import com.mapr.music.model.ArtistRate;

import java.util.List;

public interface ArtistRateDao extends MaprDbDao<ArtistRate> {

    ArtistRate getRate(String userId, String artistId);

    List<ArtistRate> getByUserId(String userId);

    List<ArtistRate> getByArtistId(String artistId);
}
