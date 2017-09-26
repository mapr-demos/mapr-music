package com.mapr.music.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.music.dao.ArtistDao;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.model.ESSearchResult;
import com.mapr.music.service.ESSearchService;
import com.mapr.music.service.PaginatedService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ESSearchServiceImpl implements ESSearchService, PaginatedService {

    /**
     * FIXME use properties file. Currently assuming that ElasticSearch is installed on the host with Wildfly(which serves the app).
     * <p>
     * ElasticSearch hostname.
     */
    public static final String HOSTNAME = "localhost";

    /**
     * ElasticSearch port number.
     */
    public static final int REST_PORT = 9200;

    private static final int PER_PAGE_DEFAULT = 5;
    private static final int FIRST_PAGE_NUM = 1;

    private static final String ARTISTS_INDEX_NAME = "artists";
    private static final String ARTISTS_TYPE_NAME = "artist";

    private static final String ALBUMS_INDEX_NAME = "albums";
    private static final String ALBUMS_TYPE_NAME = "album";

    private static final ObjectMapper mapper = new ObjectMapper();
    private RestHighLevelClient client;

    private static final Logger log = LoggerFactory.getLogger(ESSearchServiceImpl.class);


    private final ArtistDao artistDao;
    private final MaprDbDao<Album> albumDao;

    @Inject
    public ESSearchServiceImpl(@Named("artistDao") ArtistDao artistDao,
                               @Named("albumDao") MaprDbDao<Album> albumDao) {

        this.artistDao = artistDao;
        this.albumDao = albumDao;

        RestClient lowLevelRestClient = RestClient.builder(new HttpHost(HOSTNAME, REST_PORT, "http")).build();
        this.client = new RestHighLevelClient(lowLevelRestClient);
    }

    /**
     * {@inheritDoc}
     *
     * @param nameEntry specifies search query.
     * @param perPage   specifies number of search results per page. In case when value is <code>null</code> the
     *                  default value will be used. Default value depends on implementation class.
     * @param page      specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                  the first page will be returned.
     * @return list of artists and albums whose names contain specified name entry.
     */
    @Override
    public ResourceDto<ESSearchResult> findByNameEntry(String nameEntry, Integer perPage, Integer page) {
        return findByNameEntry(nameEntry, perPage, page, ARTISTS_INDEX_NAME, ALBUMS_INDEX_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @param nameEntry specifies search query.
     * @param perPage   specifies number of search results per page. In case when value is <code>null</code> the
     *                  default value will be used. Default value depends on implementation class.
     * @param page      specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                  the first page will be returned.
     * @return list of albums whose names contain specified name entry.
     */
    @Override
    public ResourceDto<ESSearchResult> findAlbumsByNameEntry(String nameEntry, Integer perPage, Integer page) {
        return findByNameEntry(nameEntry, perPage, page, ALBUMS_INDEX_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @param nameEntry specifies search query.
     * @param perPage   specifies number of search results per page. In case when value is <code>null</code> the
     *                  default value will be used. Default value depends on implementation class.
     * @param page      specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                  the first page will be returned.
     * @return list of artists whose names contain specified name entry.
     */
    @Override
    public ResourceDto<ESSearchResult> findArtistsByNameEntry(String nameEntry, Integer perPage, Integer page) {
        return findByNameEntry(nameEntry, perPage, page, ARTISTS_INDEX_NAME);
    }

    private ResourceDto<ESSearchResult> findByNameEntry(String nameEntry, Integer perPage, Integer page, String... indices) {

        if (nameEntry == null || nameEntry.isEmpty()) {
            throw new IllegalArgumentException("Name entry can not be null");
        }

        if (page == null) {
            page = FIRST_PAGE_NUM;
        }

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        if (perPage == null) {
            perPage = PER_PAGE_DEFAULT;
        }

        if (perPage <= 0) {
            throw new IllegalArgumentException("Per page value must be greater than zero");
        }

        JsonNode jsonQuery = matchQueryByName(nameEntry);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.wrapperQuery(jsonQuery.toString()));

        int offset = (page - 1) * perPage;
        sourceBuilder.from(offset);
        sourceBuilder.size(perPage);

        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(sourceBuilder);

        ResourceDto<ESSearchResult> resultPage = new ResourceDto<>();
        try {
            SearchResponse response = client.search(searchRequest);
            JsonNode result = mapper.readTree(response.toString());

            resultPage.setResults(mapToResultList(result));
            resultPage.setPagination(getPaginationInfo(page, perPage, response.getHits().getTotalHits()));
        } catch (IOException e) {
            log.warn("Can not get ES search response. Exception: {}", e);
        }

        return resultPage;
    }

    private JsonNode matchQueryByName(String name) {

        ObjectNode jsonQuery = mapper.createObjectNode();
        ObjectNode nameNode = mapper.createObjectNode();
        nameNode.put("name", name);
        jsonQuery.set("match", nameNode);

        return jsonQuery;
    }

    private List<ESSearchResult> mapToResultList(JsonNode result) {

        if (result == null) {
            return Collections.emptyList();
        }

        JsonNode hits = result.get("hits");

        int total = hits.get("total").asInt();
        if (total == 0) {
            return Collections.emptyList();
        }

        ArrayNode hitsArray = (ArrayNode) hits.get("hits");
        List<ESSearchResult> resultList = new ArrayList<>();
        for (JsonNode hit : hitsArray) {
            resultList.add(hitToResult(hit));
        }

        return resultList;
    }

    private ESSearchResult hitToResult(JsonNode hit) {

        JsonNode jsonSource = hit.get("_source");
        ESSearchResult result = mapper.convertValue(jsonSource, ESSearchResult.class);
        result.setId(hit.get("_id").asText());
        result.setType(hit.get("_type").asText());

        if (isArtistHit(hit)) {

            Artist artist = artistDao.getById(result.getId(), "profile_image_url", "slug_name", "slug_postfix");
            if (artist == null) {
                return result;
            }

            result.setImageURL(artist.getProfileImageUrl());

            String artistSlug = SlugService.constructSlugString(artist.getSlugName(), artist.getSlugPostfix());
            result.setSlug(artistSlug);

        } else if (isAlbumHit(hit)) {

            Album album = albumDao.getById(result.getId(), "cover_image_url", "slug_name", "slug_postfix");
            if (album == null) {
                return result;
            }

            result.setImageURL(album.getCoverImageUrl());

            String albumSlug = SlugService.constructSlugString(album.getSlugName(), album.getSlugPostfix());
            result.setSlug(albumSlug);
        }

        return result;
    }

    private boolean isArtistHit(JsonNode hit) {
        return ARTISTS_INDEX_NAME.equals(hit.get("_index").asText()) && ARTISTS_TYPE_NAME.equals(hit.get("_type").asText());
    }

    private boolean isAlbumHit(JsonNode hit) {
        return ALBUMS_INDEX_NAME.equals(hit.get("_index").asText()) && ALBUMS_TYPE_NAME.equals(hit.get("_type").asText());
    }

    @Override
    public long getTotalNum() {
        throw new UnsupportedOperationException();
    }

}
