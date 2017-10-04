package com.mapr.music.dao;

import com.mapr.music.model.AlbumRate;

import java.util.List;

public interface AlbumRateDao extends MaprDbDao<AlbumRate> {

    AlbumRate getRate(String userId, String albumId);

    List<AlbumRate> getByUserId(String userId);

    List<AlbumRate> getByAlbumId(String albumId);
}
