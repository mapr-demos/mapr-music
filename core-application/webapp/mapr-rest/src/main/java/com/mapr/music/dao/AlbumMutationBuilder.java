package com.mapr.music.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.model.Track;
import org.ojai.store.Connection;
import org.ojai.store.DocumentMutation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlbumMutationBuilder {

    public static final boolean SET_NULL_VALUE_DEFAULT = false;

    /**
     * Template for array fields mutation, which has the following format:
     * "array_field_name[entry_index].entry_field_name".
     */
    private static final String ARRAY_FIELD_TEMPLATE = "%s[%d].%s";

    private static final String NAME_FIELD = "name";
    private static final String BARCODE_FIELD = "barcode";
    private static final String STATUS_FIELD = "status";
    private static final String PACKAGING_FIELD = "packaging";
    private static final String LANGUAGE_FIELD = "language";
    private static final String SCRIPT_FIELD = "script";
    private static final String FORMAT_FIELD = "format";
    private static final String COUNTRY_FIELD = "country";

    private static final String TRACK_LIST_FIELD = "track_list";
    private static final String TRACK_NAME_FIELD = "name";
    private static final String TRACK_POSITION_FIELD = "position";
    private static final String TRACK_LENGTH_FIELD = "length";

    private final DocumentMutation mutation;
    private final ObjectMapper objectMapper;

    private AlbumMutationBuilder(Connection connection) {
        this.mutation = connection.newMutation();
        this.objectMapper = new ObjectMapper();
    }

    public static AlbumMutationBuilder forConnection(Connection connection) {

        if (connection == null) {
            throw new IllegalArgumentException("Connection can not be null");
        }

        return new AlbumMutationBuilder(connection);
    }

    public AlbumMutationBuilder setName(String name) {
        return setName(name, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder setName(String name, boolean setNullValue) {
        return setStringValue(NAME_FIELD, name, setNullValue);
    }

    public AlbumMutationBuilder setBarcode(String barcode) {
        return setBarcode(barcode, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder setBarcode(String barcode, boolean setNullValue) {
        return setStringValue(BARCODE_FIELD, barcode, setNullValue);
    }

    public AlbumMutationBuilder setStatus(String status) {
        return setStatus(status, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder setStatus(String status, boolean setNullValue) {
        return setStringValue(STATUS_FIELD, status, setNullValue);
    }

    public AlbumMutationBuilder setPackaging(String packaging) {
        return setPackaging(packaging, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder setPackaging(String packaging, boolean setNullValue) {
        return setStringValue(PACKAGING_FIELD, packaging, setNullValue);
    }

    public AlbumMutationBuilder setLanguage(String language) {
        return setLanguage(language, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder setLanguage(String language, boolean setNullValue) {
        return setStringValue(LANGUAGE_FIELD, language, setNullValue);
    }

    public AlbumMutationBuilder setScript(String script) {
        return setScript(script, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder setScript(String script, boolean setNullValue) {
        return setStringValue(SCRIPT_FIELD, script, setNullValue);
    }

    public AlbumMutationBuilder setFormat(String format) {
        return setFormat(format, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder setFormat(String format, boolean setNullValue) {
        return setStringValue(FORMAT_FIELD, format, setNullValue);
    }

    public AlbumMutationBuilder setCountry(String country) {
        return setCountry(country, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder setCountry(String country, boolean setNullValue) {
        return setStringValue(COUNTRY_FIELD, country, setNullValue);
    }

    public AlbumMutationBuilder setTrackList(List<Track> trackList) {

        List<Map> tracks = trackList.stream()
                .map(track -> objectMapper.convertValue(track, Map.class))
                .collect(Collectors.toList());

        this.mutation.set(TRACK_LIST_FIELD, tracks);
        return this;
    }


    public AlbumMutationBuilder addTracks(List<Track> trackList) {

        List<Map> tracks = trackList.stream()
                .map(track -> objectMapper.convertValue(track, Map.class))
                .collect(Collectors.toList());

        this.mutation.append(TRACK_LIST_FIELD, tracks);
        return this;
    }

    public AlbumMutationBuilder addTracks(Track... tracks) {
        return addTracks(Arrays.asList(tracks));
    }

    public AlbumMutationBuilder editTrack(int trackIndex, Track track) {
        return editTrack(trackIndex, track, SET_NULL_VALUE_DEFAULT);
    }

    public AlbumMutationBuilder editTrack(int trackIndex, Track track, boolean setNullValues) {

        if (track.getName() != null || setNullValues) {
            String fieldName = String.format(ARRAY_FIELD_TEMPLATE, TRACK_LIST_FIELD, trackIndex, TRACK_NAME_FIELD);
            this.mutation.set(fieldName, track.getName());
        }

        if (track.getLength() != null || setNullValues) {
            String fieldName = String.format(ARRAY_FIELD_TEMPLATE, TRACK_LIST_FIELD, trackIndex, TRACK_LENGTH_FIELD);
            this.mutation.set(fieldName, track.getLength());
        }

        if (track.getPosition() != null || setNullValues) {
            String fieldName = String.format(ARRAY_FIELD_TEMPLATE, TRACK_LIST_FIELD, trackIndex, TRACK_POSITION_FIELD);
            this.mutation.set(fieldName, track.getPosition());
        }

        return this;
    }

    public AlbumMutationBuilder deleteTrack(int trackIndex) {

        String trackEntry = String.format("%s[%d]", TRACK_LIST_FIELD, trackIndex);
        this.mutation.delete(trackEntry);
        return this;
    }

    public DocumentMutation build() {
        return this.mutation;
    }

    private AlbumMutationBuilder setStringValue(String fieldName, String value, boolean setNullValue) {

        if (value == null && !setNullValue) {
            return this;
        }

        this.mutation.set(fieldName, value);
        return this;
    }

}
