package com.mapr.music.service;

import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Artist;

import java.util.Collection;
import java.util.List;

public interface ArtistService {

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class.
     *
     * @return artists page resource.
     */
    ResourceDto<Artist> getArtistsPage();

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class.
     *
     * @param page specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *             first page will be returned.
     * @return artists page resource.
     */
    ResourceDto<Artist> getArtistsPage(Long page);

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class. artists will be ordered according to the specified order and fields.
     *
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return artists page resource.
     */
    ResourceDto<Artist> getArtistsPage(String order, List<String> orderFields);

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class. artists will be ordered according to the specified order and fields.
     *
     * @param perPage     specifies number of artists per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *                    first page will be returned.
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return artists page resource.
     */
    ResourceDto<Artist> getArtistsPage(Long perPage, Long page, String order, List<String> orderFields);

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
