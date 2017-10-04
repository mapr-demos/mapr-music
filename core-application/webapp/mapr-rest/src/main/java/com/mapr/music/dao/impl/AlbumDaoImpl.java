package com.mapr.music.dao.impl;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.AlbumMutationBuilder;
import com.mapr.music.dao.SortOption;
import com.mapr.music.model.Album;
import com.mapr.music.model.Track;
import org.ojai.Document;
import org.ojai.DocumentStream;
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
public class AlbumDaoImpl extends MaprDbDaoImpl<Album> implements AlbumDao {

    public AlbumDaoImpl() {
        super(Album.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param offset  offset value.
     * @param limit   limit value.
     * @param options define the order of documents.
     * @param lang    language code.
     * @param fields  list of fields that will present in document.
     * @return list of albums with specified language code.
     */
    @Override
    public List<Album> getByLanguage(long offset, long limit, List<SortOption> options, String lang, String... fields) {
        return processStore((connection, store) -> {

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

            return albums;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param language language code.
     * @return number of albums with specified language code.
     */
    @Override
    public long getTotalNumByLanguage(String language) {
        return processStore((connection, store) -> {

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

            // Update basic fields
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .setName(album.getName())
                    .setBarcode(album.getBarcode())
                    .setCountry(album.getCountry())
                    .setLanguage(album.getLanguage())
                    .setPackaging(album.getPackaging())
                    .setTrackList(album.getTrackList())
                    .setArtists(album.getArtists())
                    .setFormat(album.getFormat())
                    .setReleasedDate(album.getReleasedDate())
                    .setRating(album.getRating());

            // Update the OJAI Document with specified identifier
            store.update(id, mutationBuilder.build());

            Document updatedOjaiDoc = store.findById(id);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param albumId identifier of album, which is associated with track.
     * @param trackId track identifier.
     * @return single track according to the specified track identifier and album identifier.
     */
    @Override
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
     * {@inheritDoc}
     *
     * @param albumId identifier of album, whose track list will be returned.
     * @return list of tracks for the album with specified identifier.
     */
    @Override
    public List<Track> getTracksList(String albumId) {
        Album album = getById(albumId, "tracks");
        return (album == null) ? null : album.getTrackList();
    }

    /**
     * {@inheritDoc}
     *
     * @param albumId identifier of album, for which track will be added.
     * @param track   track, which will be added to the album's track list.
     * @return newly created track with id field set.
     */
    @Override
    public Track addTrack(String albumId, Track track) {

        if (!exists(albumId)) {
            return null;
        }

        track.setId(UUID.randomUUID().toString());
        processStore((connection, store) -> {

            // Add tracks to the track list
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .addTracks(track);

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutationBuilder.build());
        });

        Optional<Track> trackOptional = getTracksList(albumId).stream().filter(t -> track.getId().equals(t.getId())).findAny();
        return (trackOptional.isPresent()) ? trackOptional.get() : null;
    }

    /**
     * {@inheritDoc}
     *
     * @param albumId identifier of album, for which tracks will be added.
     * @param tracks  list of tracks, which will be added to the album's track list.
     * @return list of newly created tracks, each of tracks has id field set.
     */
    @Override
    public List<Track> addTracks(String albumId, List<Track> tracks) {

        if (!exists(albumId)) {
            return null;
        }

        tracks.forEach(track -> track.setId(UUID.randomUUID().toString()));

        processStore((connection, store) -> {

            // Add tracks to the track list
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .addTracks(tracks);

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutationBuilder.build());
        });

        Set<String> createdTracksIds = tracks.stream().map(Track::getId).collect(Collectors.toSet());
        return getTracksList(albumId).stream().filter(track -> createdTracksIds.contains(track.getId())).collect(toList());
    }

    /**
     * {@inheritDoc}
     *
     * @param albumId identifier of album, for which track will be updated.
     * @param trackId identifier of track, which will be updated.
     * @param track   contains update information.
     * @return updated track.
     */
    @Override
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

            return (trackOptional.isPresent()) ? trackOptional.get() : null;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param albumId   identifier of album, for which track list will be set.
     * @param trackList list of tracks. Some of them may correspond to existing tracks, that will be updated.
     * @return album's track list.
     */
    @Override
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

            // Set new track list for the specified album
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .setTrackList(trackList);

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutationBuilder.build());

            Document updatedOjaiDoc = store.findById(albumId, "tracks");

            // Map Ojai document to the actual instance of model class
            Album updatedAlbum = mapOjaiDocument(updatedOjaiDoc);
            return updatedAlbum.getTrackList();
        });

    }

    /**
     * {@inheritDoc}
     *
     * @param albumId identifier of album, for which track will be deleted.
     * @param trackId identifier of track, which will be deleted.
     * @return <code>true</code> if track is successfully deleted, <code>false</code> otherwise.
     */
    @Override
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

            // Delete single track
            AlbumMutationBuilder mutationBuilder = AlbumMutationBuilder.forConnection(connection)
                    .deleteTrack(trackIndex);

            // Update the OJAI Document with specified identifier
            store.update(albumId, mutationBuilder.build());
            return true;
        });

    }

    /**
     * {@inheritDoc}
     *
     * @param nameEntry specifies query criteria.
     * @param limit     specified limit.
     * @param fields    specifies fields that will be fetched.
     * @return list of albums which titles start with the specified name entry.
     */
    @Override
    public List<Album> getByNameStartsWith(String nameEntry, long limit, String... fields) {
        return processStore((connection, store) -> {

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
