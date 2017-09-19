package com.mapr.music.service.impl;

import com.mapr.music.dao.AlbumDao;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.ojai.FieldPath;
import org.ojai.KeyValue;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

@Startup
@Singleton
public class ArtistsChangelogListenerService {

    private static String CHANGE_LOG = "/mapr_music_artists_changelog:artists";

    private static final Logger log = LoggerFactory.getLogger(ArtistsChangelogListenerService.class);

    @Resource(lookup = "java:jboss/ee/concurrency/factory/MaprMusicThreadFactory")
    private ManagedThreadFactory threadFactory;

    @Inject
    @Named("albumDao")
    private AlbumDao albumDao;

    /**
     * Consumer used to consume MapR-DB CDC events.
     */
    private KafkaConsumer<byte[], ChangeDataRecord> consumer;

    public ArtistsChangelogListenerService() {

        Properties consumerProperties = new Properties();
        consumerProperties.setProperty("group.id", "cdc.consumer.demo_table.fts_geo");
        consumerProperties.setProperty("enable.auto.commit", "true");
        consumerProperties.setProperty("auto.offset.reset", "latest");
        consumerProperties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumerProperties.setProperty("value.deserializer", "com.mapr.db.cdc.ChangeDataRecordDeserializer");

        loginTestUser("mapr", "mapr");
        consumer = new KafkaConsumer<>(consumerProperties);
    }

    @PostConstruct
    public void init() {

        log.info("Subscribing to the artists Changelog '{}'", CHANGE_LOG);
        consumer.subscribe(Collections.singletonList(CHANGE_LOG));

        // TODO RUN
        Thread thread = threadFactory.newThread(() -> {
            log.error("RUN IN ANOTHER THREAD");

            while (true) {
                ConsumerRecords<byte[], ChangeDataRecord> changeRecords = consumer.poll(500);
                Iterator<ConsumerRecord<byte[], ChangeDataRecord>> iter = changeRecords.iterator();

                while (iter.hasNext()) {
                    ConsumerRecord<byte[], ChangeDataRecord> crec = iter.next();
                    // The ChangeDataRecord contains all the changes made to a document
                    ChangeDataRecord changeDataRecord = crec.value();
                    String documentId = changeDataRecord.getId().getString();

                    if (changeDataRecord.getType() == ChangeDataRecordType.RECORD_INSERT) {
                        log.error("\n\t Document Inserted '{}'", documentId);


                    } else if (changeDataRecord.getType() == ChangeDataRecordType.RECORD_UPDATE) {
                        log.error("\n\t Document Updated '{}'", documentId);


                    } else if (changeDataRecord.getType() == ChangeDataRecordType.RECORD_DELETE) {
                        log.error("\n\t Document Deleted '{}'", documentId);
//                        changeDataRecord.iterator()

                        Iterator<KeyValue<FieldPath, ChangeNode>> cdrItr = changeDataRecord.iterator();
                        log.error("\n\t ITERATOR HAS NEXT: '{}'", cdrItr.hasNext());

                        Map.Entry<FieldPath, ChangeNode> changeNodeEntry = cdrItr.next();
                        String fieldPathAsString = changeNodeEntry.getKey().asPathString();
                        log.error("\n\t fieldPathAsString: '{}'", fieldPathAsString);

                        ChangeNode changeNode = changeNodeEntry.getValue();
                        log.error("\n\t changeNode: '{}'", changeNode);

//                        Map<String, Object> documentInserted = changeNode.getMap();
                        log.error("\n\t VALUE: '{}'", changeNode.getValue());

                    }

                }
            }
        });
        thread.start();
    }

    private static void loginTestUser(String username, String group) {
        UserGroupInformation currentUgi = UserGroupInformation.createUserForTesting(username, new String[]{group});
        UserGroupInformation.setLoginUser(currentUgi);
    }
}
