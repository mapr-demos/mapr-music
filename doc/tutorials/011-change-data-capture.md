# Change Data Capture(CDC)

The Change Data Capture (CDC) system allows you to capture changes made to data records in MapR-DB tables 
(JSON or binary). These data changes are the result of inserts, updates, and deletions and are called change data 
records. Once the change data records are propagated to a topic, a MapR-ES/Kafka consumer application is used to read 
and process them.

## MapR Music Application CDC use cases
#### Statistics
MapR Music App uses CDC in order to maintain `/apps/statistics` table, which contains statistics information about 
Albums/Artists tables.
[REST Service](https://github.com/mapr-demos/mapr-music/tree/devel/mapr-rest) has 
[CdcStatisticService](https://github.com/mapr-demos/mapr-music/blob/devel/mapr-rest/src/main/java/com/mapr/music/service/ArtistsChangelogListenerService.java) 
class, which listens Artist's and Album's changelog and updates `/apps/statistics` table.
Service run in separate thread using `ManagedThreadFactory`, which must be configured at Wildfly. Below you can see code 
snippet which is responsible of updating `/apps/statistics` table
```
@Startup
@Singleton
public class CdcStatisticService implements StatisticService {
    
    @Resource(lookup = THREAD_FACTORY)
    private ManagedThreadFactory threadFactory;

    ...
    
    static class ChangeDataRecordHandler implements Runnable {
    
        private static long KAFKA_CONSUMER_POLL_TIMEOUT = 500L;

        interface Action {
            void handle(String documentId);
        }

        KafkaConsumer<byte[], ChangeDataRecord> consumer;
        Action onInsert;
        Action onDelete;

        ChangeDataRecordHandler(KafkaConsumer<byte[], ChangeDataRecord> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            while (true) {

                ConsumerRecords<byte[], ChangeDataRecord> changeRecords = consumer.poll(KAFKA_CONSUMER_POLL_TIMEOUT);
                for (ConsumerRecord<byte[], ChangeDataRecord> consumerRecord : changeRecords) {

                    // The ChangeDataRecord contains all the changes made to a document
                    ChangeDataRecord changeDataRecord = consumerRecord.value();
                    String documentId = changeDataRecord.getId().getString();

                    // Handle 'RECORD_INSERT'
                    if (changeDataRecord.getType() == ChangeDataRecordType.RECORD_INSERT && this.onInsert != null) {
                        this.onInsert.handle(documentId);
                    }

                    // Handle 'RECORD_DELETE'
                    if (changeDataRecord.getType() == ChangeDataRecordType.RECORD_DELETE && this.onDelete != null) {
                        this.onDelete.handle(documentId);
                    }

                }
            }
        }

        public void setOnInsert(Action onInsert) {
            this.onInsert = onInsert;
        }

        public void setOnDelete(Action onDelete) {
            this.onDelete = onDelete;
        }

    }
    
    ...
    
    @PostConstruct
    public void init() {

        ...
        
        // Create and adjust consumer which is used to consume MapR-DB CDC events for Albums table.
        KafkaConsumer<byte[], ChangeDataRecord> albumsChangelogConsumer = new KafkaConsumer<>(consumerProperties);
        albumsChangelogConsumer.subscribe(Collections.singletonList(ALBUMS_CHANGE_LOG));
        ChangeDataRecordHandler albumsHandler = new ChangeDataRecordHandler(albumsChangelogConsumer);
        albumsHandler.setOnDelete((id) -> decrementAlbums());
        albumsHandler.setOnInsert((id) -> incrementAlbums());

        // Create and adjust consumer which is used to consume MapR-DB CDC events for Artists table.
        KafkaConsumer<byte[], ChangeDataRecord> artistsChangelogConsumer = new KafkaConsumer<>(consumerProperties);
        artistsChangelogConsumer.subscribe(Collections.singletonList(ARTISTS_CHANGE_LOG));
        ChangeDataRecordHandler artistsHandler = new ChangeDataRecordHandler(artistsChangelogConsumer);
        artistsHandler.setOnDelete((id) -> decrementArtists());
        artistsHandler.setOnInsert((id) -> incrementArtists());

        threadFactory.newThread(albumsHandler).start();
        threadFactory.newThread(artistsHandler).start();
    }
        
    ...
}
```

#### Artists deletion
Artists deletion is performed using CDC. MapR Music 
[REST Service](https://github.com/mapr-demos/mapr-music/tree/devel/mapr-rest) has 
[ArtistsChangelogListenerService](https://github.com/mapr-demos/mapr-music/blob/devel/mapr-rest/src/main/java/com/mapr/music/service/CdcStatisticService.java) 
class, which listens Artist's changelog and deletes Artist and it's rates, albums. 
Service is also run in separate thread using `ManagedThreadFactory`. Below you can find code snippet, which implements 
actual Artist deletion logic:
```
  ...
  
  threadFactory.newThread(() -> {
              while (true) {
  
                  ConsumerRecords<byte[], ChangeDataRecord> changeRecords = consumer.poll(KAFKA_CONSUMER_POLL_TIMEOUT);
                  for (ConsumerRecord<byte[], ChangeDataRecord> consumerRecord : changeRecords) {
  
                      // The ChangeDataRecord contains all the changes made to a document
                      ChangeDataRecord changeDataRecord = consumerRecord.value();
  
                      // Ignore all the records that is not 'RECORD_UPDATE'
                      if (changeDataRecord.getType() != ChangeDataRecordType.RECORD_UPDATE) {
                          continue;
                      }
  
                      String artistId = changeDataRecord.getId().getString();
                      // Use the ChangeNode to capture all the individual changes
                      for (Map.Entry<FieldPath, ChangeNode> changeNodeEntry : changeDataRecord) {
  
                          String fieldPathAsString = changeNodeEntry.getKey().asPathString();
                          ChangeNode changeNode = changeNodeEntry.getValue();
  
                          // When "INSERTING" a document the field path is empty (new document)
                          // and all the changes are made in a single object represented as a Map
                          if (fieldPathAsString == null || fieldPathAsString.isEmpty()) {
                              // Ignore artist inserting
                              continue;
                          }
  
                          // Ignore all the fields except of artist's 'deleted' flag
                          if (!"deleted".equalsIgnoreCase(fieldPathAsString)) {
                              continue;
                          }
  
                          // Ignore change record for this Artist if 'deleted' flag changed to 'false'
                          if (!changeNode.getBoolean()) {
                              break;
                          }
  
                          Artist artistToDelete = artistDao.getById(artistId);
  
                          // Artist does not exist
                          if (artistToDelete == null) {
                              break;
                          }
  
                          if (artistToDelete.getAlbums() != null) {
                              artistToDelete.getAlbums().stream()
                                      .filter(Objects::nonNull)
                                      .map(Album.ShortInfo::getId)
                                      .map(albumDao::getById)
                                      .filter(Objects::nonNull)
                                      .filter(album -> album.getArtists() != null)
                                      .peek(album -> { // Remove artist from album's list of artists
                                          List<Artist.ShortInfo> toRemove = album.getArtists().stream()
                                                  .filter(Objects::nonNull)
                                                  .filter(artist -> artistId.equals(artist.getId()))
                                                  .collect(Collectors.toList());
  
                                          album.getArtists().removeAll(toRemove);
                                      })
                                      .forEach(album -> {
                                          if (album.getArtists().isEmpty()) { // Remove albums that had only one artist
                                              // Remove Album's rates
                                              albumRateDao.getByAlbumId(album.getId())
                                                      .forEach(rate -> albumRateDao.deleteById(rate.getId()));
                                              albumDao.deleteById(album.getId());
                                          } else {
                                              albumDao.update(album.getId(), album);
                                          }
                                      });
                          }
  
                          // Remove Artist's rates
                          artistRateDao.getByArtistId(artistId).forEach(rate -> artistRateDao.deleteById(rate.getId()));
  
                          artistDao.deleteById(artistId);
                          log.info("Artist with id = '{}' is deleted", artistId);
                      }
                  }
              }
  
          }).start();
    
    ...

```

#### Elastic Search Integration
CDC is used in Elastic Search integration. MapR Music 
[Elastic Search Service](https://github.com/mapr-demos/mapr-music/tree/devel/elasticsearch-service) listens changelogs 
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
