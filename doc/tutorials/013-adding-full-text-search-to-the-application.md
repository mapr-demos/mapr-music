# Adding Full Text Search to the Application

It is very common for applications to require some advanced search capabilities, such as insentitive case search, fuzzy search and many more. These full text search capabilties are generally well suited for MapR-DB's query and index capabilities, however one can combine MapR-DB with an indexing engine, such as Elasticsearch, designed specifically for full text search. To accomplish this, Elasticsearch listens to MapR-DB's Change Data Capture (CDC) stream. Anytime a CRUD operation is performed on a table in MapR-DB, Elasticsearch is notified via the CDC stream and updates its own indices accordingly. Anytime an end-user does search, the web app (e.g. JBoss) will direct that request to the Elasticsearch search API.

Elasticsearch listens for requests on port 9200. This is configurable in [mapr-music/mapr-rest/src/main/java/com/mapr/music/util/MaprProperties.java](https://github.com/mapr-demos/mapr-music/blob/32a2e66b6874d6ad01d8defc485595b70b4ef596/mapr-rest/src/main/java/com/mapr/music/util/MaprProperties.java). Anytime you search for an album or artist in the MapR Music webapp, your search results will come from Elasticsearch.  You can also query Elasticsearch with `curl`, like this:

```
# Show all indices
curl -X GET "localhost:9200/_cat/indices?v"
# Search for Wyclef
curl -X GET "localhost:9200/artists/_search?q=name:wyclef&pretty"
```

## Indexing MapR-DB attributes in Elasticsearch

[Elastic Search Service](https://github.com/mapr-demos/mapr-music/tree/master/elasticsearch-service) listens changelogs 
and publishes the changes to the ElasticSearch.

MapR Elastic Search Service implementation is based on `ChangelogListener`:

```java 
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

```java 
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

This CDC consumer, sends the artists name and albums names with the document _id to Elasticsearch for indexing. Also when an artist or albums is deleted from the index. The `MaprElasticSearchServiceBuilder.java` class is used to update the Elasticsearch index.

```java 
public class MaprElasticSearchServiceBuilder {

    ...
    // update the index
    IndexResponse response = client.prepareIndex(indexName, typeName, documentId)
        .setSource(allowed.toString(), XContentType.JSON)
        .get();
    ...

    ...
    // Delete the value in the index
    ...
    client.prepareDelete(indexName, typeName, id).get());
    ...

}
```

## Querying Elasticsearch and MapR-DB

Once you have data in MapR-DB and Elasticsearch, the application must call the Elasticsearch server to do a full text search.

The `ESSearchService.java` is the class used to achieve this.

```java 
public class ESSearchService implements PaginatedService {

    ...
        JsonNode jsonQuery = matchQueryByName(nameEntry);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.wrapperQuery(jsonQuery.toString()));

        int offset = (page - 1) * perPage;
        sourceBuilder.from(offset);
        sourceBuilder.size(perPage);

        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(sourceBuilder);
    ...

}
```

---
Next: [Creating Recommendation Engine with Spark ML lib](014-creating-recommendation-engine.md)
