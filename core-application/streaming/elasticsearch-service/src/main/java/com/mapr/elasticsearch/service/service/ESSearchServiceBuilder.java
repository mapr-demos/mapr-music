package com.mapr.elasticsearch.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class ESSearchServiceBuilder {

    public interface SearchService {

        JsonNode search(JsonNode jsonQuery);

        void close();
    }

    private class SearchServiceImpl implements SearchService {

        TransportClient client;
        ObjectMapper mapper = new ObjectMapper();

        Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

        SearchServiceImpl() {
            // Create ElasticSearch Client
            this.client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(inetAddress, port));
        }

        @Override
        public JsonNode search(JsonNode jsonQuery) {

            if (jsonQuery == null) {
                throw new IllegalArgumentException("JSON Query can not be null");
            }

            if (this.client == null) {
                throw new IllegalStateException("ElasticSearch client is closed");
            }

            QueryBuilder query = QueryBuilders.wrapperQuery(jsonQuery.toString());

            SearchResponse response = client.prepareSearch(indexNames.toArray(new String[indexNames.size()]))
                    .setTypes(typeNames.toArray(new String[typeNames.size()]))
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(query)
                    .get();

            JsonNode result = null;
            try {
                result = mapper.readTree(response.toString());
            } catch (IOException e) {
                log.warn("Can not parse ES response '{}' as JSON. Exception: {}", response.toString(), e);
            }

            return result;
        }

        @Override
        public void close() {

            if (this.client != null) {
                client.close();
                client = null;
            }
        }
    }

    /**
     * ElasticSearch specific fields.
     */
    private Integer port;
    private InetAddress inetAddress;
    private Set<String> indexNames;
    private Set<String> typeNames;

    public ESSearchServiceBuilder() {
    }

    /**
     * Specifies ElasticSearch port number.
     *
     * @param port ElasticSearch port number.
     * @return builder.
     */
    public ESSearchServiceBuilder withPort(int port) {

        if (port <= 0) {
            throw new IllegalArgumentException("Port must be greater than zero");
        }

        this.port = port;
        return this;
    }

    /**
     * Specifies ElasticSearch hostname.
     *
     * @param hostname ElasticSearch hostname.
     * @return builder.
     */
    public ESSearchServiceBuilder withHostname(String hostname) {

        if (hostname == null || hostname.isEmpty()) {
            throw new IllegalArgumentException("Hostname can not be empty");
        }

        try {
            this.inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }

        return this;
    }

    /**
     * Specifies ElasticSearch index name.
     *
     * @param indexName ElasticSearch index name
     * @return builder.
     */
    public ESSearchServiceBuilder withIndexName(String indexName) {

        if (indexName == null || indexName.isEmpty()) {
            throw new IllegalArgumentException("Index name can not be empty");
        }

        if (this.indexNames == null) {
            this.indexNames = new HashSet<>();
        }

        this.indexNames.add(indexName);
        return this;
    }

    /**
     * Specifies ElasticSearch type name.
     *
     * @param typeName ElasticSearch type name.
     * @return builder.
     */
    public ESSearchServiceBuilder withTypeName(String typeName) {

        if (typeName == null || typeName.isEmpty()) {
            throw new IllegalArgumentException("Type name can not be empty");
        }

        if (this.typeNames == null) {
            this.typeNames = new HashSet<>();
        }

        this.typeNames.add(typeName);
        return this;
    }

    /**
     * Builds the {@link ESSearchServiceBuilder} according to the specified properties.
     *
     * @return instence of {@link ESSearchServiceBuilder}, adjusted according to the specified fields.
     * @throws IllegalStateException in case when some of the required properties are missed.
     */
    public SearchService build() {

        ensureFieldNonNull("port", this.port);
        ensureFieldNonNull("hostname", this.inetAddress);
        ensureFieldNonNull("indexNames", this.indexNames);
        ensureFieldNonNull("typeNames", this.typeNames);

        return new SearchServiceImpl();
    }

    private void ensureFieldNonNull(String fieldName, Object fieldValue) {
        if (fieldValue == null) {
            String message = String.format("Field '%s' must be set", fieldName);
            throw new IllegalStateException(message);
        }
    }

}
