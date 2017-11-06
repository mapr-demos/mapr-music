package com.mapr.elasticsearch.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.elasticsearch.service.listener.ChangelogListener;
import com.mapr.elasticsearch.service.listener.impl.ChangelogListenerImpl;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Builder for the {@link MaprElasticSearchService}.
 */
public class MaprElasticSearchServiceBuilder {

    /**
     * Declares interface for MapR ElasticSearch service, which  listens changes to the MapR table table and publish
     * them to the specified ElasticSearch cluster.
     */
    public interface MaprElasticSearchService {
        void start();
    }

    /**
     * Specifies ES index creating/updating action on Change Data Record callback.
     */
    private class SaveIndexCDCCallback implements ChangelogListener.ChangeDataRecordCallback {

        TransportClient client;
        ObjectMapper mapper = new ObjectMapper();

        Logger log = LoggerFactory.getLogger(SaveIndexCDCCallback.class);

        SaveIndexCDCCallback(TransportClient client) {
            this.client = client;
        }

        @Override
        public void callback(String documentId, JsonNode changes) {

            JsonNode allowed = copyOnlyAllowedFields(changes);

            if (allowed == null) {
                log.info("Document with id: '{}' was changed, but none of the fields are allowed to be sent to the ES",
                        documentId);
                return;
            }

            IndexResponse response = client.prepareIndex(indexName, typeName, documentId)
                    .setSource(allowed.toString(), XContentType.JSON)
                    .get();

            log.info("Elasticsearch Index Response: '{}'", response);
        }

        /**
         * Only specified fields will be sent to the ElasticSearch.
         *
         * @param original all the changes.
         * @return changes for the specified fields.
         */
        private JsonNode copyOnlyAllowedFields(JsonNode original) {

            ObjectNode allowed = null;
            Iterator<String> fieldNamesIterator = original.fieldNames();
            while (fieldNamesIterator.hasNext()) {

                String fieldName = fieldNamesIterator.next();
                if (!fields.contains(fieldName)) {
                    continue;
                }

                if (allowed == null) {
                    allowed = mapper.createObjectNode();
                }

                allowed.set(fieldName, original.get(fieldName));
            }

            return allowed;
        }
    }

    /**
     * ElasticSearch specific fields.
     */
    private Integer port;
    private InetAddress inetAddress;
    private String indexName;
    private String typeName;

    /**
     * MapR Changelog path in '/stream:topic' format
     */
    private String changelog;

    /**
     * Set of fields that will be sent to the ElasticSearch
     */
    private Set<String> fields;

    public MaprElasticSearchServiceBuilder() {
    }

    /**
     * Specifies ElasticSearch port number.
     *
     * @param port ElasticSearch port number.
     * @return builder.
     */
    public MaprElasticSearchServiceBuilder withPort(int port) {

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
    public MaprElasticSearchServiceBuilder withHostname(String hostname) {

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
    public MaprElasticSearchServiceBuilder withIndexName(String indexName) {

        if (indexName == null || indexName.isEmpty()) {
            throw new IllegalArgumentException("Index name can not be empty");
        }

        this.indexName = indexName;
        return this;
    }

    /**
     * Specifies ElasticSearch type name.
     *
     * @param typeName ElasticSearch type name.
     * @return builder.
     */
    public MaprElasticSearchServiceBuilder withTypeName(String typeName) {

        if (typeName == null || typeName.isEmpty()) {
            throw new IllegalArgumentException("Type name can not be empty");
        }

        this.typeName = typeName;
        return this;
    }

    /**
     * Specifies path to the MapR changelog, from which changes will be published to the ElasticSearch. Changelog path
     * has the following format: '/stream_name:topic_name'.
     *
     * @param changelog changelog path in following format: '/stream_name:topic_name'.
     * @return builder.
     */
    public MaprElasticSearchServiceBuilder withChangelog(String changelog) {

        if (changelog == null || changelog.isEmpty()) {
            throw new IllegalArgumentException("Changelog can not be empty");
        }

        if (!changelog.startsWith("/") || !changelog.contains(":")) {
            throw new IllegalArgumentException("Changelog path must start with '/' and contain ':' character as " +
                    "stream-topic separator");
        }

        this.changelog = changelog;
        return this;
    }

    /**
     * Adds field name to the set of allowed fields. Only changes to the allowed fields will be sent to the
     * ElasticSearch.
     *
     * @param fieldName MapR-DB document's field name.
     * @return builder.
     */
    public MaprElasticSearchServiceBuilder withField(String fieldName) {

        if (fieldName == null || fieldName.isEmpty()) {
            throw new IllegalArgumentException("Field name can not be empty");
        }

        if (this.fields == null) {
            this.fields = new HashSet<>();
        }

        this.fields.add(fieldName);
        return this;
    }

    /**
     * Adds field names to the set of allowed fields. Only changes to the allowed fields will be sent to the
     * ElasticSearch.
     *
     * @param fieldNames MapR-DB document's field names.
     * @return builder.
     */
    public MaprElasticSearchServiceBuilder withFields(String... fieldNames) {


        if (fieldNames == null) {
            throw new IllegalArgumentException("Field names can not be null");
        }

        if (this.fields == null) {
            this.fields = new HashSet<>();
        }

        this.fields.addAll(Arrays.asList(fieldNames));
        return this;
    }

    /**
     * Builds the {@link MaprElasticSearchService} according to the specified properties.
     *
     * @return instence of {@link MaprElasticSearchService}, which can be started via {@link MaprElasticSearchService#start()}.
     * @throws IllegalStateException in case when some of the required properties are missed.
     */
    public MaprElasticSearchService build() {

        ensureFieldNonNull("port", this.port);
        ensureFieldNonNull("hostname", this.inetAddress);
        ensureFieldNonNull("indexName", this.indexName);
        ensureFieldNonNull("typeName", this.typeName);
        ensureFieldNonNull("changelog", this.changelog);
        ensureFieldNonNull("fields", this.fields);

        return () -> {

            // Create ES Client
            TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(inetAddress, port));

            // Create CDC Listener
            ChangelogListener listener = ChangelogListenerImpl.forChangelog(changelog);

            // Set 'onInsert' callback
            listener.onInsert(new SaveIndexCDCCallback(client));

            // Set 'onUpdate' callback
            listener.onUpdate(new SaveIndexCDCCallback(client));

            // Define and set 'onDelete' callback
            listener.onDelete((id) -> client.prepareDelete(indexName, typeName, id).get());

            listener.listen();

        };
    }

    private void ensureFieldNonNull(String fieldName, Object fieldValue) {
        if (fieldValue == null) {
            String message = String.format("Field '%s' must be set", fieldName);
            throw new IllegalStateException(message);
        }
    }
}
