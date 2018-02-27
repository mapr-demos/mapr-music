# Change Data Capture(CDC)

The Change Data Capture (CDC) allows you to capture changes made to data records in MapR-DB tables 
(JSON or binary). These data changes are the result of inserts, updates, and deletions and are called change data 
records. Once the change data records are propagated to a topic, a MapR-ES/Kafka consumer application is used to read 
and process them.

You can find more information about CDC in the documentation: [Change Data Capture](https://maprdocs.mapr.com/home/MapR-DB/DB-ChangeData/changeData-overview.html)

## MapR Music Application CDC use cases

MapR Music application use CDC in various places:

* Delete albums and ratings when an Artists is deleted
* Calculate table statistics 
* Index some fields in Elasticsearch)


#### Artists deletion

MapR Music 
[REST Service](https://github.com/mapr-demos/mapr-music/tree/master/mapr-rest) has 
[ArtistsChangelogListenerService](https://github.com/mapr-demos/mapr-music/blob/master/mapr-rest/src/main/java/com/mapr/music/service/CdcStatisticService.java) 
class, which listens Artist's changelog and deletes Artist and it's ratings, albums. 

This Service is executed in a separate thread using `ManagedThreadFactory`, which must be configured in Wildfly (we have done that in a previous step). Below you can find code snippet, which implements 
actual Artist deletion logic:

```java 
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

#### Statistics

MapR Music App uses CDC in order to maintain `/apps/statistics` table, which contains statistics information about 
Albums/Artists tables.
[REST Service](https://github.com/mapr-demos/mapr-music/tree/master/mapr-rest) has 
[CdcStatisticService](https://github.com/mapr-demos/mapr-music/blob/master/mapr-rest/src/main/java/com/mapr/music/service/ArtistsChangelogListenerService.java) 
class, which listens Artist's and Album's changelog and updates `/apps/statistics` table.

This service ervice is executed in a separate thread using `ManagedThreadFactory`, which must be configured in Wildfly (we have done that in a previous step). Below you can see code  snippet which is responsible of updating `/apps/statistics` table

```java 
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

In the next step you will learn how to use CDC to index documents values from MapR-DB JSON into Elasticseach to provide a Full Text Search feature. 

---
Next: [Adding Full Text Search to the Application](013-adding-full-text-search-to-the-application'.md)