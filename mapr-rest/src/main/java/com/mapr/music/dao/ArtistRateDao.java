package com.mapr.music.dao;

import com.google.common.base.Stopwatch;
import com.mapr.music.model.ArtistRate;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.DocumentMutation;
import org.ojai.store.Query;
import org.ojai.store.QueryCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArtistRateDao extends MaprDbDao<ArtistRate> {

    public ArtistRateDao() {
        super(ArtistRate.class);
    }

    /**
     * Returns Artist rate according to the specified user identifier and artist identifier.
     *
     * @param userId   user identifier.
     * @param artistId artist identifier.
     * @return artist rate.
     */
    public ArtistRate getRate(String userId, String artistId) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();
            QueryCondition condition = connection.newCondition()
                    .and()
                    .is("user_id", QueryCondition.Op.EQUAL, userId)
                    .is("document_id", QueryCondition.Op.EQUAL, artistId)
                    .close()
                    .build();

            Query query = connection.newQuery().where(condition).build();

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            Iterator<Document> documentIterator = documentStream.iterator();

            if (!documentIterator.hasNext()) {
                return null;
            }

            log.info("Get rate by artist id '{}' and user id '{}' took {}", artistId, userId, stopwatch);

            return mapOjaiDocument(documentIterator.next());
        });
    }

    /**
     * Returns list of Artist rates by user identifier.
     *
     * @param userId user's identifier.
     * @return list of Artist rates.
     */
    public List<ArtistRate> getByUserId(String userId) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();
            Query query = connection.newQuery().where(
                    connection.newCondition()
                            .is("user_id", QueryCondition.Op.EQUAL, userId)
                            .build()
            ).build();

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            List<ArtistRate> rates = new ArrayList<>();
            for (Document document : documentStream) {
                ArtistRate rate = mapOjaiDocument(document);
                if (rate != null) {
                    rates.add(rate);
                }
            }

            log.info("Get '{}' rates by user id '{}' took {}", rates.size(), userId, stopwatch);

            return rates;
        });
    }

    /**
     * Returns list of Artist rates by artist identifier.
     *
     * @param artistId artist's identifier.
     * @return list of Artist rates.
     */
    public List<ArtistRate> getByArtistId(String artistId) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();
            Query query = connection.newQuery().where(
                    connection.newCondition()
                            .is("document_id", QueryCondition.Op.EQUAL, artistId)
                            .build()
            ).build();

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            List<ArtistRate> rates = new ArrayList<>();
            for (Document document : documentStream) {
                ArtistRate rate = mapOjaiDocument(document);
                if (rate != null) {
                    rates.add(rate);
                }
            }

            log.info("Get '{}' rates by artist id '{}' took {}", rates.size(), artistId, stopwatch);

            return rates;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param id         identifier of document, which will be updated.
     * @param artistRate artist rate.
     * @return updated artist rate.
     */
    @Override
    public ArtistRate update(String id, ArtistRate artistRate) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Create a DocumentMutation to update non-null fields
            DocumentMutation mutation = connection.newMutation();

            // Update only non-null fields
            if (artistRate.getRating() != null) {
                mutation.set("rating", artistRate.getRating());
            }

            // Update the OJAI Document with specified identifier
            store.update(id, mutation);

            Document updatedOjaiDoc = store.findById(id);

            log.info("Update document from table '{}' with id: '{}'. Elapsed time: {}", tablePath, id, stopwatch);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }
}
