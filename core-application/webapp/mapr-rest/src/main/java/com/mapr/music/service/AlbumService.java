package com.mapr.music.service;

import com.mapr.music.model.Album;

import java.util.List;

/**
 * Album service interface which is defines methods that should implement actual business logic.
 */
public interface AlbumService {

    /**
     * Returns list of all albums.
     *
     * @return list of all albums.
     */
    List<Album> getAlbumsList();

    /**
     * Returns single album according to it's identifier.
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    Album getById(String id);

    // TODO sort options
}
