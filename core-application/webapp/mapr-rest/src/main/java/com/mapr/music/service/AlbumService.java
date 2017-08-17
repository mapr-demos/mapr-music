package com.mapr.music.service;

import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;

/**
 * Album service interface which is defines methods that should implement actual business logic.
 */
public interface AlbumService {

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class.
     *
     * @return albums page resource.
     */
    ResourceDto<Album> getAlbumsPage();

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class.
     *
     * @param page specifies number of page, which will be returned.
     * @return albums page resource.
     */
    ResourceDto<Album> getAlbumsPage(long page);

    /**
     * Returns single album according to it's identifier.
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    Album getById(String id);

    // TODO sort options
}
