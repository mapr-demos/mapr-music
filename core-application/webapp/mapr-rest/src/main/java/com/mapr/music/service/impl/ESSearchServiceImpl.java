package com.mapr.music.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.elasticsearch.service.service.ESSearchServiceBuilder;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.model.ESSearchResult;
import com.mapr.music.service.ESSearchService;

public class ESSearchServiceImpl implements ESSearchService {

    /**
     * FIXME use properties file. Currently assuming that ElasticSearch is installed on the host with Wildfly(which serves the app).
     * <p>
     * ElasticSearch hostname.
     */
    public static final String HOSTNAME = "localhost";

    /**
     * ElasticSearch port number.
     */
    public static final int PORT = 9300;

    private static final String ARTISTS_INDEX_NAME = "artists";
    private static final String ARTISTS_TYPE_NAME = "artist";

    private static final String ALBUMS_INDEX_NAME = "albums";
    private static final String ALBUMS_TYPE_NAME = "album";

    private static final ObjectMapper mapper = new ObjectMapper();

    private ESSearchServiceBuilder.SearchService actualService;

    public ESSearchServiceImpl() {

        actualService = new ESSearchServiceBuilder()
                .withHostname(HOSTNAME)
                .withPort(PORT)
                .withIndexName(ARTISTS_INDEX_NAME) // Search Artists
                .withIndexName(ALBUMS_INDEX_NAME) // and Albums
                .withTypeName(ARTISTS_TYPE_NAME)
                .withTypeName(ALBUMS_TYPE_NAME)
                .build();
    }

    @Override
    public ESSearchResult findByNameEntry(String nameEntry) {

        if (nameEntry == null || nameEntry.isEmpty()) {
            throw new IllegalArgumentException("Name entry can not be null");
        }

        JsonNode query = matchQueryByName(nameEntry);
        JsonNode result = actualService.search(query);

        return mapToResult(result);
    }

    private JsonNode matchQueryByName(String name) {

        ObjectNode jsonQuery = mapper.createObjectNode();
        ObjectNode nameNode = mapper.createObjectNode();
        nameNode.put("name", name);
        jsonQuery.set("match", nameNode);

        return jsonQuery;
    }

    private ESSearchResult mapToResult(JsonNode result) {

        JsonNode hits = result.get("hits");

        int total = hits.get("total").asInt();
        if (total == 0) {
            return new ESSearchResult();
        }

        ESSearchResult esSearchResult = new ESSearchResult();
        esSearchResult.setTotal(total);

        ArrayNode hitsArray = (ArrayNode) hits.get("hits");
        for (JsonNode hit : hitsArray) {

            if (isAlbumHit(hit)) {
                esSearchResult.addAlbum(hitToAlbum(hit));
            } else if (isArtistHit(hit)) {
                esSearchResult.addArtist(hitToArtist(hit));
            }
        }

        return esSearchResult;
    }

    private Artist hitToArtist(JsonNode hit) {

        JsonNode source = hit.get("_source");
        Artist artist = mapper.convertValue(source, Artist.class);
        artist.setId(hit.get("_id").asText());

        return artist;
    }

    private Album hitToAlbum(JsonNode hit) {

        JsonNode source = hit.get("_source");
        Album album = mapper.convertValue(source, Album.class);
        album.setId(hit.get("_id").asText());

        return album;
    }

    private boolean isArtistHit(JsonNode hit) {
        return ARTISTS_INDEX_NAME.equals(hit.get("_index").asText()) && ARTISTS_TYPE_NAME.equals(hit.get("_type").asText());
    }

    private boolean isAlbumHit(JsonNode hit) {
        return ALBUMS_INDEX_NAME.equals(hit.get("_index").asText()) && ALBUMS_TYPE_NAME.equals(hit.get("_type").asText());
    }

}
