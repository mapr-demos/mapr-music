package com.mapr.music.dao;

import com.mapr.music.model.Artist;

import java.util.List;

public interface ArtistDao extends MaprDbDao<Artist> {

    /**
     * Finds artists, which names start with the specified name entry.
     *
     * @param nameEntry specifies query criteria.
     * @param limit     specified limit.
     * @param fields    specifies fields that will be fetched.
     * @return list of artists which names start with the specified name entry.
     */
    List<Artist> getByNameStartsWith(String nameEntry, long limit, String... fields);

}
