package com.mapr.music.service;

import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.dto.TrackDto;
import com.mapr.music.model.Language;

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
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class. Albums will be ordered according to the specified list of sort options.
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param sortOptions specifies albums ordering.
     * @return albums page resource.
     */
    ResourceDto<AlbumDto> getAlbumsPage(Long perPage, Long page, List<SortOption> sortOptions);

    /**
     * Returns list of albums by language code. List of albums is represented by page with default number of albums.
     * Default number of albums depends on implementation class. Albums will be ordered according to the specified list
     * of sort options.
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param sortOptions specifies albums ordering.
     * @param lang        language code.
     * @return albums page resource.
     */
    ResourceDto<AlbumDto> getAlbumsPageByLanguage(Long perPage, Long page, List<SortOption> sortOptions, String lang);

    /**
     * Returns single album according to it's identifier.
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    AlbumDto getAlbumById(String id);

    /**
     * Returns single album by it's slug name.
     *
     * @param slugName slug representation of album's name which is used to generate readable and SEO-friendly URLs.
     * @return album with specified slug name.
     */
    AlbumDto getAlbumBySlugName(String slugName);

    /**
     * Deletes single album by it's identifier.
     *
     * @param id identifier of album which will be deleted.
     */
    void deleteAlbumById(String id);

    /**
     * Creates album according to the specified instance of {@link AlbumDto} class.
     *
     * @param albumDto contains album info.
     * @return created album.
     */
    AlbumDto createAlbum(AlbumDto albumDto);

    /**
     * Updates single album according to the specified instance of {@link AlbumDto} class.
     *
     * @param albumDto album which will be updated. Note, that album's id must be set, otherwise
     *                 {@link IllegalArgumentException} will be thrown.
     * @return updated album.
     * @throws IllegalArgumentException in case when specified album is <code>null</code> or it does not contain id.
     */
    AlbumDto updateAlbum(AlbumDto albumDto);

    /**
     * Updates single album according to the specified instance of {@link AlbumDto} class.
     *
     * @param id       identifier of album which will be updated.
     * @param albumDto album which will be updated.
     * @return updated album.
     */
    AlbumDto updateAlbum(String id, AlbumDto albumDto);

    /**
     * Returns single track according to the specified track identifier and album identifier.
     *
     * @param id      identifier of album, which is associated with track.
     * @param trackId track identifier.
     * @return single track according to the specified track identifier and album identifier.
     */
    TrackDto getTrackById(String id, String trackId);

    /**
     * Returns list of tracks for the album with specified identifier.
     *
     * @param id identifier of album, whose track list will be returned.
     * @return list of tracks for the album with specified identifier.
     */
    List<TrackDto> getAlbumTracksList(String id);

    /**
     * Adds single track to the album with specified identifier.
     *
     * @param id    identifier of album, for which track will be added.
     * @param track track, which will be added to the album's track list.
     * @return newly created track with id field set.
     */
    TrackDto addTrackToAlbumTrackList(String id, TrackDto track);

    /**
     * Adds list of tracks to the album with specified identifier.
     *
     * @param id     identifier of album, for which tracks will be added.
     * @param tracks list of tracks, which will be added to the album's track list.
     * @return list of newly created tracks, each of tracks has id field set.
     */
    List<TrackDto> addTracksToAlbumTrackList(String id, List<TrackDto> tracks);

    /**
     * Updates single track.
     *
     * @param id      identifier of album, for which track will be updated.
     * @param trackId identifier of track, which will be updated.
     * @param track   contains update information.
     * @return updated track.
     */
    TrackDto updateAlbumTrack(String id, String trackId, TrackDto track);

    /**
     * Sets track list for the album with specified identifier. Note, that in case when track's identifier corresponds
     * to the existing track, track will be updated, otherwise new track will be created.
     *
     * @param id        identifier of album, for which track list will be set.
     * @param trackList list of tracks. Some of them may correspond to existing tracks, that will be updated.
     * @return album's track list.
     */
    List<TrackDto> setAlbumTrackList(String id, List<TrackDto> trackList);

    /**
     * Deletes single track according to the specified album identifier and track identifier.
     *
     * @param id      identifier of album, for which track will be deleted.
     * @param trackId identifier of track, which will be deleted.
     */
    void deleteAlbumTrack(String id, String trackId);

    /**
     * Returns list of supported albums languages.
     *
     * @return list of supported albums languages.
     */
    List<Language> getSupportedAlbumsLanguages();

    /**
     * Search albums according to the specified name entry. Returns albums which titles start with name entry.
     *
     * @param nameEntry specifies search criteria.
     * @param limit     specifies number of albums, which will be returned. Can be overridden by actual service
     *                  implementation.
     * @return list of albums which titles start with name entry.
     */
    List<AlbumDto> searchAlbums(String nameEntry, Long limit);

}
