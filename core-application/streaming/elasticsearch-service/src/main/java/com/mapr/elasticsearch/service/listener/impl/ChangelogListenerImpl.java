package com.mapr.elasticsearch.service.listener.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.elasticsearch.service.listener.ChangelogListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.ojai.FieldPath;
import org.ojai.KeyValue;
import org.ojai.Value;
import org.ojai.store.cdc.ChangeDataRecord;
import org.ojai.store.cdc.ChangeDataRecordType;
import org.ojai.store.cdc.ChangeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public final class ChangelogListenerImpl implements ChangelogListener {

    private static final long KAFKA_CONSUMER_POLL_TIMEOUT = 500L;

    /**
     * Consumer used to consume MapR-DB CDC events.
     */
    private KafkaConsumer<byte[], ChangeDataRecord> consumer;

    /**
     * MapR changelog path in '/stream:topic' format.
     */
    private String changelog;

    private ChangeDataRecordCallback onInsert;
    private ChangeDataRecordCallback onUpdate;
    private Consumer<String> onDelete;

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private ChangelogListenerImpl(String changelog, Properties consumerProperties) {
        this.consumer = new KafkaConsumer<>(consumerProperties);
        this.changelog = changelog;
    }

    public static ChangelogListenerImpl forChangelog(String changelog) {

        if (changelog == null || changelog.isEmpty()) {
            throw new IllegalArgumentException("Changelog path can not be empty");
        }

        if (!changelog.startsWith("/") || !changelog.contains(":")) {
            throw new IllegalArgumentException("Changelog path must start with '/' and contain ':' character as " +
                    "stream-topic separator");
        }

        Properties consumerProperties = new Properties();
        consumerProperties.setProperty("enable.auto.commit", "true");
        consumerProperties.setProperty("auto.offset.reset", "latest");
        consumerProperties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumerProperties.setProperty("value.deserializer", "com.mapr.db.cdc.ChangeDataRecordDeserializer");

        return new ChangelogListenerImpl(changelog, consumerProperties);
    }


    @Override
    public void onInsert(ChangeDataRecordCallback callback) {
        this.onInsert = callback;
    }

    @Override
    public void onUpdate(ChangeDataRecordCallback callback) {
        this.onUpdate = callback;
    }

    @Override
    public void onDelete(Consumer<String> callback) {
        this.onDelete = callback;
    }

    @Override
    public void listen() {

        if (this.onInsert == null && this.onUpdate == null && this.onDelete == null) {
            log.warn("There is no callbacks set. Listening change data records without callbacks has no effect.");
        }

        this.consumer.subscribe(Collections.singletonList(this.changelog));
        log.info("Start listening changelog '{}'", this.changelog);

        new Thread(() -> {
            while (true) {

                ConsumerRecords<byte[], ChangeDataRecord> changeRecords = consumer.poll(KAFKA_CONSUMER_POLL_TIMEOUT);
                for (ConsumerRecord<byte[], ChangeDataRecord> consumerRecord : changeRecords) {

                    // The ChangeDataRecord contains all the changes made to a document
                    ChangeDataRecord changeDataRecord = consumerRecord.value();
                    ChangeDataRecordType recordType = changeDataRecord.getType();
                    switch (recordType) {
                        case RECORD_INSERT:
                            handleInsert(changeDataRecord);
                            break;
                        case RECORD_UPDATE:
                            handleUpdate(changeDataRecord);
                            break;
                        case RECORD_DELETE:
                            handleDelete(changeDataRecord);
                            break;
                        default:
                            log.warn("Get record of unknown type '{}'. Ignoring ...", recordType);
                    }
                }

            }
        }).start();
    }

    private void handleInsert(ChangeDataRecord changeDataRecord) {

        String documentId = changeDataRecord.getId().getString();
        log.debug("Inserted document with id = '{}'", documentId);

        if (this.onInsert == null) {
            return;
        }

        Iterator<KeyValue<FieldPath, ChangeNode>> iterator = changeDataRecord.iterator();
        if (!iterator.hasNext()) {
            log.warn("Insert Change Data Record received with no change nodes. Ignoring ...");
            return;
        }

        Map.Entry<FieldPath, ChangeNode> changeNodeEntry = iterator.next();
        ChangeNode changeNode = changeNodeEntry.getValue();
        if (changeNode == null) {
            log.warn("Insert Change Data Record received with 'null' change node. Ignoring ...");
            return;
        }

        Value changeNodeValue = changeNode.getValue();
        if (changeNodeValue == null) {
            log.warn("Insert Change Data Record received with 'null' change node value. Ignoring ...");
            return;
        }

        String jsonString = changeNodeValue.asJsonString();
        this.onInsert.callback(documentId, parseJsonString(jsonString));
    }

    private void handleUpdate(ChangeDataRecord changeDataRecord) {

        String documentId = changeDataRecord.getId().getString();
        log.debug("Updated document with id = '{}'", documentId);

        if (this.onUpdate == null) {
            return;
        }

        ObjectNode changes = mapper.createObjectNode();
        for (Map.Entry<FieldPath, ChangeNode> changeNodeEntry : changeDataRecord) {

            ChangeNode changeNode = changeNodeEntry.getValue();
            String jsonString = changeNode.getValue().asJsonString();
            String fieldPathAsString = changeNodeEntry.getKey().asPathString();
            changes.set(fieldPathAsString, parseJsonString(jsonString));
        }

        this.onUpdate.callback(documentId, changes);
    }

    private void handleDelete(ChangeDataRecord changeDataRecord) {

        String deletedDocumentId = changeDataRecord.getId().getString();
        log.debug("Deleted document with id = '{}'", deletedDocumentId);
        if (this.onDelete != null) {
            this.onDelete.accept(deletedDocumentId);
        }
    }

    private JsonNode parseJsonString(String jsonString) {

        JsonNode node = null;
        try {
            node = mapper.readValue(jsonString, JsonNode.class);
        } catch (IOException e) {
            log.warn("Can not parse JSON string '{}' as instance of Jackson JsonNode", jsonString);

        }

        return (node != null) ? node : mapper.createObjectNode();
    }
}
