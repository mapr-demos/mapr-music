package com.mapr.music.dao;

import com.google.common.base.Stopwatch;
import com.mapr.music.dao.AlbumMutationBuilder;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.SortOption;
import com.mapr.music.model.Album;
import com.mapr.music.model.Track;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.DocumentMutation;
import org.ojai.store.Query;
import org.ojai.store.QueryCondition;
import org.ojai.store.SortOrder;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Actual implementation of {@link com.mapr.music.dao.MaprDbDao} to manage {@link Album} model.
 */
@Named("albumDao")
public class AlbumDao extends MaprDbDao<Album> {

    public AlbumDao() {
        super(Album.class);
    }

    /**
     * Returns list of albums by language code.
     *
     * @param offset  offset value.
     * @param limit   limit value.
     * @param options define the order of documents.
     * @param lang    language code.
     * @param fields  list of fields that will present in document.
     * @return list of albums with specified language code.
     */
    public List<Album> getByLanguage(long offset, long limit, List<SortOption> options, String lang, String... fields) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            Query query = connection.newQuery();

            // Select only specified field
            if (fields != null && fields.length > 0) {
                query.select(fields);
            } else {
                query.select("*");
            }

            // Build Query Condition to fetch documents by specified language
            QueryCondition languageEqualsCondition = connection.newCondition()
                    .is("language", QueryCondition.Op.EQUAL, lang)
                    .build();

            // Add Condition to the Query
            query.where(languageEqualsCondition);

            // Add ordering if sort options specified
            if (options != null && !options.isEmpty()) {
                for (SortOption opt : options) {
                    SortOrder ojaiOrder = (SortOption.Order.DESC == opt.getOrder()) ? SortOrder.DESC : SortOrder.ASC;
                    for (String field : opt.getFields()) {
                        query = query.orderBy(field, ojaiOrder);
                    }
                }
            }

            // Add specified offset and limit to the Query
            query.offset(offset)
                    .limit(limit)
                    .build();

            DocumentStream documentStream = store.findQuery(query);
            List<Album> albums = new ArrayList<>();
            for (Document doc : documentStream) {
                albums.add(mapOjaiDocument(doc));
            }

            log.info("Get list of '{}' documents from '{}' table by language: '{}' with offset: '{}', limit: '{}', " +
                            "sortOptions: '{}', fields: '{}'. Elapsed time: {}", albums.size(), tablePath, lang, offset,
                    limit, options, (fields != null) ? Arrays.asList(fields) : "[]", stopwatch);

            return albums;
        });
    }

    /**
     * Returns number of albums according to the specified language.
     *
     * @param language language code.
     * @return number of albums with specified language code.
     */
    public long getTotalNumByLanguage(String language) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            QueryCondition languageEqualsCondition = connection.newCondition()
                    .is("language", QueryCondition.Op.EQUAL, language)
                    .build();

            Query query = connection.newQuery()
                    .select("_id")
                    .where(languageEqualsCondition)
                    .build();

            DocumentStream documentStream = store.findQuery(query);
            long totalNum = 0;
            for (Document ignored : documentStream) {
                totalNum++;
            }

            log.info("Counting '{}' albums by language '{}' took {}", totalNum, language, stopwatch);

            return totalNum;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param album contains info for album, which will be created.
     * @return created album.
     */
    @Override
    public Album create(Album album) {

        String id = UUID.randomUUID().toString();
        album.setId(id);

        return super.create(album);
    }

    /**
     * {@inheritDoc}
     *
     * @param id    identifier of album, which will be updated.
     * @param album contains album info that will be updated.
     * @return updated album.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Album update(String id, Album album) {

        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Update basic fields
            DocumentMutation albumMutation = AlbumMutationBuilder.forConnection(connection)
                    .setName(album.getName())
                    .setBarcode(album.getBarcode())
                    .setCountry(album.getCountry())
                    .setLanguage(album.getLanguage())
                    .setPackaging(album.getPackaging())
                    .setTrackList(album.getTrackList())
                    .setArtists(album.getArtists())
                    .setFormat(album.getFormat())
                    .setReleasedDate(album.getReleasedDate())
                    .setRating(album.getRating())
                    .build();

            // Set update info if available
            getUpdateInfo().ifPresent(updateInfo -> albumMutation.set("update_info", updateInfo));

            // Update the OJAI Document with specified identifier
            store.update(id, albumMutation);

            Document updatedOjaiDoc = store.findById(id);

            log.info("Update document from table '{}' with id: '{}'. Elapsed time: {}", tablePath, id, stopwatch);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }

    /**
     * Returns single track according to the specified track identifier and album identifier.
     *
     * @param albumId identifier of album, which is associated with track.
     * @param trackId track identifier.
     * @return single track according to the specified track identifier and album identifier.
     */
    public Track getTrackById(String albumId, String trackId) {

        List<Track> existingAlbumTracks = getTracksList(albumId);
        if (existingAlbumTracks == null) {
            return null;
        }

        Optional<Track> trackOptional = existingAlbumTracks.stream()
                .filter(track -> trackId.equals(track.getId()))
                .findAny();

        return (trackOptional.isPresent()) ? trackOptional.get() : null;
    }

    /**
     * Returns list of tracks for the album with specified identifier.
     *
     * @param albumId identifier of album, whose track list will be returned.
     * @return list of tracks for the album with specified identifier.
     */
    public List<Track> getTracksList(String albumId) {
        Album album = getById(albumId, "tracks");
        return (album == null) ? null : album.getTrackList();
    }

    /**
     * Adds single track to the album with specified identifier.
     *
     * @param albumId identifier of album, for which track will be added.
     * @param track   track, which will be added to the album's track list.
     * @return newly created track with id field set.
     */
    public Track addTrack(String albumId, Track track) {

        if (!exists(albumId)) {
            return null;
        }

        track.setId(UUID.randomUUID().toString());
        processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Add tracks to the track list
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .addTracks(track);

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutationBuilder.build());

            log.info("Add track to album '{}' took {}", albumId, stopwatch);

        });

        Optional<Track> trackOptional = getTracksList(albumId).stream().filter(t -> track.getId().equals(t.getId())).findAny();
        return (trackOptional.isPresent()) ? trackOptional.get() : null;
    }

    /**
     * Adds list of tracks to the album with specified identifier.
     *
     * @param albumId identifier of album, for which tracks will be added.
     * @param tracks  list of tracks, which will be added to the album's track list.
     * @return list of newly created tracks, each of tracks has id field set.
     */
    public List<Track> addTracks(String albumId, List<Track> tracks) {

        if (!exists(albumId)) {
            return null;
        }

        tracks.forEach(track -> track.setId(UUID.randomUUID().toString()));

        processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Add tracks to the track list
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .addTracks(tracks);

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutationBuilder.build());

            log.info("Add '{}' tracks to album '{}' took {}", tracks.size(), albumId, stopwatch);

        });

        Set<String> createdTracksIds = tracks.stream().map(Track::getId).collect(Collectors.toSet());
        return getTracksList(albumId).stream().filter(track -> createdTracksIds.contains(track.getId())).collect(toList());
    }

    /**
     * Updates single track.
     *
     * @param albumId identifier of album, for which track will be updated.
     * @param trackId identifier of track, which will be updated.
     * @param track   contains update information.
     * @return updated track.
     */
    public Track updateTrack(String albumId, String trackId, Track track) {

        List<Track> existingAlbumTracks = getTracksList(albumId);
        if (existingAlbumTracks == null) {
            return null;
        }

        int trackIndex = getTrackIndexById(existingAlbumTracks, trackId);
        if (trackIndex < 0) {
            return null;
        }

        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Update single track
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .editTrack(trackIndex, track);

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutationBuilder.build());

            Document updatedOjaiDoc = store.findById(albumId, "tracks");

            // Map Ojai document to the actual instance of model class
            Album updatedAlbum = mapOjaiDocument(updatedOjaiDoc);
            Optional<Track> trackOptional = updatedAlbum.getTrackList().stream()
                    .filter(t -> trackId.equals(t.getId()))
                    .findAny();

            log.info("Updating album's track with id '{}' for albumId: '{}' took {}", trackId, albumId, stopwatch);

            return (trackOptional.isPresent()) ? trackOptional.get() : null;
        });
    }

    /**
     * Sets track list for the album with specified identifier. Note, that in case when track's identifier corresponds
     * to the existing track, track will be updated, otherwise new track will be created.
     *
     * @param albumId   identifier of album, for which track list will be set.
     * @param trackList list of tracks. Some of them may correspond to existing tracks, that will be updated.
     * @return album's track list.
     */
    public List<Track> setTrackList(String albumId, List<Track> trackList) {

        List<Track> existingTracks = getTracksList(albumId);
        if (existingTracks == null) {
            return null;
        }

        Set<String> existingTracksIds = existingTracks.stream().map(Track::getId).collect(Collectors.toSet());

        // Set identifiers for tracks that don't have one
        trackList.stream()
                .filter(track -> track.getId() == null || !existingTracksIds.contains(track.getId()))
                .forEach(track -> track.setId(UUID.randomUUID().toString()));

        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Set new track list for the specified album
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .setTrackList(trackList);

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutationBuilder.build());

            Document updatedOjaiDoc = store.findById(albumId, "tracks");

            // Map Ojai document to the actual instance of model class
            Album updatedAlbum = mapOjaiDocument(updatedOjaiDoc);

            log.info("Updating album's track list for albumId: '{}' took {}", albumId, stopwatch);

            return updatedAlbum.getTrackList();
        });

    }

    /**
     * Deletes single track according to the specified album identifier and track identifier.
     *
     * @param albumId identifier of album, for which track will be deleted.
     * @param trackId identifier of track, which will be deleted.
     * @return <code>true</code> if track is successfully deleted, <code>false</code> otherwise.
     */
    public boolean deleteTrack(String albumId, String trackId) {

        List<Track> existingAlbumTracks = getTracksList(albumId);
        if (existingAlbumTracks == null) {
            return false;
        }

        int trackIndex = getTrackIndexById(existingAlbumTracks, trackId);
        if (trackIndex < 0) {
            return false;
        }

        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Delete single track
            DocumentMutation mutation = AlbumMutationBuilder.forConnection(connection)
                    .deleteTrack(trackIndex).build();

            // Set update info if available
            getUpdateInfo().ifPresent(updateInfo -> mutation.set("update_info", updateInfo));

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutation);

            log.info("Deleting album's track with id: '{}' for albumId: '{}' took {}", trackId, albumId, stopwatch);

            return true;
        });

    }

    /**
     * Finds albums, which titles start with the specified name entry.
     *
     * @param nameEntry specifies query criteria.
     * @param limit     specified limit.
     * @param fields    specifies fields that will be fetched.
     * @return list of albums which titles start with the specified name entry.
     */
    public List<Album> getByNameStartsWith(String nameEntry, long limit, String... fields) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();
            Query query = connection.newQuery();

            // Select only specified field
            if (fields != null && fields.length > 0) {
                query.select(fields);
            } else {
                query.select("*");
            }

            // Build Query Condition to fetch documents by name entry
            String nameStartsWithPattern = nameEntry + "%";
            QueryCondition nameStartsWithCondition = connection.newCondition()
                    .like("name", nameStartsWithPattern)
                    .build();

            // Add Condition and specified limit to the Query
            query.where(nameStartsWithCondition)
                    .limit(limit)
                    .build();

            DocumentStream documentStream = store.findQuery(query);
            List<Album> albums = new ArrayList<>();
            for (Document doc : documentStream) {
                albums.add(mapOjaiDocument(doc));
            }

            log.info("Get '{}' albums by name entry: '{}' with limit: '{}', fields: '{}'. Elapsed time: {}",
                    albums.size(), nameEntry, limit, (fields != null) ? Arrays.asList(fields) : "[]", stopwatch);

            return albums;
        });
    }

    private int getTrackIndexById(List<Track> trackList, String trackId) {

        for (int i = 0; i < trackList.size(); i++) {
            if (trackId.equals(trackList.get(i).getId())) {
                return i;
            }
        }

        return -1;
    }
}
