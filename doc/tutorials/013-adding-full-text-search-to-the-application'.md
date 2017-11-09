# Adding Full Text Search to the Application

It is very common for applications to require some advanced search capabilities, that could not be covered by MapR-DB querying and indexing features. This includes, 
insentitive case search, fuzzy search and many more.

A option for this is to use an indexing engine, like Elasticsearch, and send the attributed to use from MapR-DB Table to Elasticsearch when a document is inserting, updated or deleted.

This is another place where the Change Data Capture introduce in the previous step could be used.

[Elastic Search Service](https://github.com/mapr-demos/mapr-music/tree/master/elasticsearch-service) listens changelogs 
and publishes the changes to the ElasticSearch.

MapR Elastic Search Service implementation is based on `ChangelogListener`:
```
public interface ChangelogListener {

    interface ChangeDataRecordCallback {
        void callback(String documentId, JsonNode changes);
    }

    void onInsert(ChangeDataRecordCallback callback);

    void onUpdate(ChangeDataRecordCallback callback);

    void onDelete(Consumer<String> callback);

    void listen();
}
```

Actual implementation is based on `KafkaConsumer`, which listens changelog:
```
public final class ChangelogListenerImpl implements ChangelogListener {

    /**
     * Consumer used to consume MapR-DB CDC events.
     */
    private KafkaConsumer<byte[], ChangeDataRecord> consumer;

    ...
    
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
        
    ...
    
}
```
