package com.mapr.music.dao;

import com.mapr.music.model.Album;

import java.util.List;

/**
 * Album Data Access Object interface, which defines methods for obtaining 'Album' documents from MapR DB.
 */
public interface AlbumDao {

    /**
     * Returns list of albums according to specified <code>offset</code> and <code>limit</code> values.
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @return list of albums.
     */
    List<Album> getAlbumList(long offset, long limit);

    /**
     * Returns list of albums according to specified <code>offset</code> and <code>limit</code> values using projection.
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @param fields list of fields that will present in album.
     * @return list of albums.
     */
    List<Album> getAlbumList(long offset, long limit, String... fields);

    /**
     * Returns single album by it's identifier.
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    Album getById(String id);

    /**
     * Returns single album by it's identifier using projection. Note that only specified fields will be filled with
     * values.
     *
     * @param id     album's identifier.
     * @param fields list of fields that will present in album.
     * @return album with the specified identifier.
     */
    Album getById(String id, String... fields);

    /**
     * Counts total number of albums.
     *
     * @return total number of albums.
     */
    long getTotalNum();

    // TODO sort options
}
