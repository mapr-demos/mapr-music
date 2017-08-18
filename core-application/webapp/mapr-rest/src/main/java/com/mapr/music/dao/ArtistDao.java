package com.mapr.music.dao;

import com.mapr.music.model.Artist;

import java.util.List;

public interface ArtistDao {
    /**
     * @param offset
     * @param limit
     * @return
     */
    List<Artist> getAll(long offset, long limit);

    /**
     * @param offset
     * @param limit
     * @param fields
     * @return
     */
    List<Artist> getAll(long offset, long limit, String... fields);

    /**
     * @param id
     * @return
     */
    Artist getById(String id);

    /**
     * @param id
     * @param fields
     * @return
     */
    Artist getById(String id, String... fields);

    long getTotalNum();

    // TODO sort options
}
