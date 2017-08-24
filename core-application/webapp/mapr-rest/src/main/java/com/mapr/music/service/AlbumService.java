package com.mapr.music.service;

import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;

import java.util.List;

/**
 * Album service interface which is defines methods that should implement actual business logic.
 */
public interface AlbumService {

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class.
     *
     * @return first albums page resource.
     */
    ResourceDto<AlbumDto> getAlbumsPage();

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class.
     *
     * @param page specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *             first page will be returned.
     * @return albums page resource.
     */
    ResourceDto<AlbumDto> getAlbumsPage(Long page);

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class. Albums will be ordered according to the specified order and fields.
     *
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return albums page resource.
     */
    ResourceDto<AlbumDto> getAlbumsPage(String order, List<String> orderFields);

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class. Albums will be ordered according to the specified order and fields.
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *                    first page will be returned.
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return albums page resource.
     */
    ResourceDto<AlbumDto> getAlbumsPage(Long perPage, Long page, String order, List<String> orderFields);

    /**
     * Returns single album according to it's identifier.
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    AlbumDto getAlbumById(String id);

    /**
     * Deletes single album by it's identifier.
     *
     * @param id identifier of album which will be deleted.
     */
    void deleteAlbumById(String id);

    /**
     * Creates album according to the specified instance of {@link Album} class.
     *
     * @param album contains album info.
     * @return created album.
     */
    AlbumDto createAlbum(Album album);

    /**
     * Updates single album according to the specified instance of {@link Album} class.
     *
     * @param album album which will be updated. Note, that album's id must be set, otherwise
     *              {@link IllegalArgumentException} will be thrown.
     * @return updated album.
     * @throws IllegalArgumentException in case when specified album is <code>null</code> or it does not contain id.
     */
    AlbumDto updateAlbum(Album album);

    /**
     * Updates single album according to the specified instance of {@link Album} class.
     *
     * @param id    identifier of album which will be updated.
     * @param album album which will be updated.
     * @return updated album.
     */
    AlbumDto updateAlbum(String id, Album album);

}
