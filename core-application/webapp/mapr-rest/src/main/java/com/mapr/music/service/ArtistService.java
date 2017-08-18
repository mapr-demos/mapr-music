package com.mapr.music.service;

import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Artist;

import java.util.Collection;

public interface ArtistService {

    /**
     * @return
     */
    ResourceDto<Artist> getAll();

    ResourceDto<Artist> getAll(long page);

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

    Artist getById(String id, Collection<String> fields);

    // TODO sort options
}
