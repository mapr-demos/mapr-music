package com.mapr.elasticsearch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.elasticsearch.service.service.MaprElasticSearchServiceBuilder;

import java.io.IOException;

public class Main {

    private static final String MAPR_MUSIC_CHANGELOG = "/mapr_music_changelog";
    private static final String ARTISTS_CHANGELOG = MAPR_MUSIC_CHANGELOG + ":artists";
    private static final String ARTISTS_INDEX_NAME = "artists";
    private static final String ARTISTS_TYPE_NAME = "artist";

    private static final String ALBUMS_CHANGELOG = MAPR_MUSIC_CHANGELOG + ":albums";
    private static final String ALBUMS_INDEX_NAME = "albums";
    private static final String ALBUMS_TYPE_NAME = "album";

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9300;

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        // Build and start service for the Artists table
        new MaprElasticSearchServiceBuilder()
                .withHostname(HOSTNAME)
                .withPort(PORT)
                .withIndexName(ARTISTS_INDEX_NAME)
                .withTypeName(ARTISTS_TYPE_NAME)
                .withChangelog(ARTISTS_CHANGELOG)
                .withField("name") // only Artist's name will be sent to the ElasticSearch
                .build().start();

        // Build and start service for the Albums table
        new MaprElasticSearchServiceBuilder()
                .withHostname(HOSTNAME)
                .withPort(PORT)
                .withIndexName(ALBUMS_INDEX_NAME)
                .withTypeName(ALBUMS_TYPE_NAME)
                .withChangelog(ALBUMS_CHANGELOG)
                .withField("name") // only Album's name will be sent to the ElasticSearch
                .build().start();


    }

}
