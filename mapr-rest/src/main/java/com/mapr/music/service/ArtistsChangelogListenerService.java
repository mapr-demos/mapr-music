package com.mapr.music.service;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.AlbumRateDao;
import com.mapr.music.dao.ArtistDao;
import com.mapr.music.dao.ArtistRateDao;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.ojai.FieldPath;
import org.ojai.store.cdc.ChangeDataRecord;
import org.ojai.store.cdc.ChangeDataRecordType;
import org.ojai.store.cdc.ChangeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.mapr.music.util.MaprProperties.*;

@Startup
@Singleton
public class ArtistsChangelogListenerService {

    private static long KAFKA_CONSUMER_POLL_TIMEOUT = 500L;

    private static final Logger log = LoggerFactory.getLogger(ArtistsChangelogListenerService.class);

    /**
     * Consumer used to consume MapR-DB CDC events.
     */
    private KafkaConsumer<byte[], ChangeDataRecord> consumer;

    @Resource(lookup = "java:jboss/ee/concurrency/factory/MaprMusicThreadFactory")
    private ManagedThreadFactory threadFactory;

    @Inject
    @Named("albumDao")
    private AlbumDao albumDao;

    @Inject
    @Named("artistDao")
    private ArtistDao artistDao;

    @Inject
    private AlbumRateDao albumRateDao;

    @Inject
    private ArtistRateDao artistRateDao;

    @PostConstruct
    public void init() {

        Properties consumerProperties = new Properties();
        consumerProperties.setProperty("group.id", "mapr.music.artists.listener");
        consumerProperties.setProperty("enable.auto.commit", "true");
        consumerProperties.setProperty("auto.offset.reset", "latest");
        consumerProperties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumerProperties.setProperty("value.deserializer", "com.mapr.db.cdc.ChangeDataRecordDeserializer");

        loginTestUser(MAPR_USER_NAME, MAPR_USER_GROUP);
        consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Collections.singletonList(ARTISTS_CHANGE_LOG));

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
    }

    private static void loginTestUser(String username, String group) {
        UserGroupInformation currentUgi = UserGroupInformation.createUserForTesting(username, new String[]{group});
        UserGroupInformation.setLoginUser(currentUgi);
    }
}
