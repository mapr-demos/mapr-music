package com.mapr.music.service;

import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Artist;

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
     * Returns single artist according to it's identifier.
     *
     * @param id artist's identifier.
     * @return artist with the specified identifier.
     */
    Artist getById(String id);

    /**
     * Deletes single artist by it's identifier.
     *
     * @param id identifier of artist which will be deleted.
     */
    void deleteArtistById(String id);

    /**
     * Creates artist according to the specified instance of {@link Artist} class.
     *
     * @param artist contains artist info.
     * @return created artist.
     */
    Artist createArtist(Artist artist);

    /**
     * Creates artist according to the specified JSON string.
     *
     * @param jsonString contains album info.
     * @return created artist.
     */
    Artist createArtist(String jsonString);

    /**
     * Updates single artist according to the specified instance of {@link Artist} class.
     *
     * @param artist artist which will be updated. Note, that artist's id must be set, otherwise
     *               {@link IllegalArgumentException} will be thrown.
     * @return updated artist.
     * @throws IllegalArgumentException in case when specified artist is <code>null</code> or it does not contain id.
     */
    Artist updateArtist(Artist artist);

    /**
     * Updates single artist according to the specified instance of {@link Artist} class.
     *
     * @param id     identifier of artist which will be updated.
     * @param artist artist which will be updated.
     * @return updated artist.
     */
    Artist updateArtist(String id, Artist artist);
}
