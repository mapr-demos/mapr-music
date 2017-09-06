package com.mapr.music.dao;

import com.mapr.music.model.Album;
import com.mapr.music.model.Track;

import java.util.List;

public interface AlbumDao extends MaprDbDao<Album> {

    /**
     * Returns list of albums by language code.
     *
     * @param offset      offset value.
     * @param limit       limit value.
     * @param sortOptions define the order of documents.
     * @param lang        language code.
     * @param fields      list of fields that will present in document.
     * @return list of albums with specified language code.
     */
    List<Album> getByLanguage(long offset, long limit, List<SortOption> sortOptions, String lang, String... fields);

    /**
     * Returns number of albums according to the specified language.
     *
     * @param language language code.
     * @return number of albums with specified language code.
     */
    long getTotalNumByLanguage(String language);

    /**
     * Returns single track according to the specified track identifier and album identifier.
     *
     * @param albumId identifier of album, which is associated with track.
     * @param trackId track identifier.
     * @return single track according to the specified track identifier and album identifier.
     */
    Track getTrackById(String albumId, String trackId);

    /**
     * Returns list of tracks for the album with specified identifier.
     *
     * @param albumId identifier of album, whose track list will be returned.
     * @return list of tracks for the album with specified identifier.
     */
    List<Track> getTracksList(String albumId);

    /**
     * Adds single track to the album with specified identifier.
     *
     * @param albumId identifier of album, for which track will be added.
     * @param track   track, which will be added to the album's track list.
     * @return newly created track with id field set.
     */
    Track addTrack(String albumId, Track track);

    /**
     * Adds list of tracks to the album with specified identifier.
     *
     * @param albumId identifier of album, for which tracks will be added.
     * @param tracks  list of tracks, which will be added to the album's track list.
     * @return list of newly created tracks, each of tracks has id field set.
     */
    List<Track> addTracks(String albumId, List<Track> tracks);

    /**
     * Updates single track.
     *
     * @param albumId identifier of album, for which track will be updated.
     * @param trackId identifier of track, which will be updated.
     * @param track   contains update information.
     * @return updated track.
     */
    Track updateTrack(String albumId, String trackId, Track track);

    /**
     * Sets track list for the album with specified identifier. Note, that in case when track's identifier corresponds
     * to the existing track, track will be updated, otherwise new track will be created.
     *
     * @param albumId   identifier of album, for which track list will be set.
     * @param trackList list of tracks. Some of them may correspond to existing tracks, that will be updated.
     * @return album's track list.
     */
    List<Track> setTrackList(String albumId, List<Track> trackList);

    /**
     * Deletes single track according to the specified album identifier and track identifier.
     *
     * @param albumId identifier of album, for which track will be deleted.
     * @param trackId identifier of track, which will be deleted.
     * @return <code>true</code> if track is successfully deleted, <code>false</code> otherwise.
     */
    boolean deleteTrack(String albumId, String trackId);

}
