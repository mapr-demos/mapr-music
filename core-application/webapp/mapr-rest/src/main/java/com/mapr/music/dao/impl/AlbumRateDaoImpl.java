package com.mapr.music.dao.impl;

import com.mapr.music.dao.AlbumRateDao;
import com.mapr.music.model.AlbumRate;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.DocumentMutation;
import org.ojai.store.Query;
import org.ojai.store.QueryCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AlbumRateDaoImpl extends MaprDbDaoImpl<AlbumRate> implements AlbumRateDao {

    public AlbumRateDaoImpl() {
        super(AlbumRate.class);
    }

    @Override
    public AlbumRate getRate(String userId, String albumId) {
        return processStore((connection, store) -> {

            QueryCondition condition = connection.newCondition()
                    .and()
                    .is("user_id", QueryCondition.Op.EQUAL, userId)
                    .is("document_id", QueryCondition.Op.EQUAL, albumId)
                    .close()
                    .build();

            Query query = connection.newQuery().where(condition).build();

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            Iterator<Document> documentIterator = documentStream.iterator();

            if (!documentIterator.hasNext()) {
                return null;
            }

            return mapOjaiDocument(documentIterator.next());
        });
    }

    @Override
    public List<AlbumRate> getByUserId(String userId) {
        return processStore((connection, store) -> {

            Query query = connection.newQuery().where(
                    connection.newCondition()
                            .is("user_id", QueryCondition.Op.EQUAL, userId)
                            .build()
            ).build();

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            List<AlbumRate> rates = new ArrayList<>();
            for (Document document : documentStream) {
                AlbumRate rate = mapOjaiDocument(document);
                if (rate != null) {
                    rates.add(rate);
                }
            }

            return rates;
        });
    }

    @Override
    public List<AlbumRate> getByAlbumId(String albumId) {
        return processStore((connection, store) -> {

            Query query = connection.newQuery().where(
                    connection.newCondition()
                            .is("document_id", QueryCondition.Op.EQUAL, albumId)
                            .build()
            ).build();

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            List<AlbumRate> rates = new ArrayList<>();
            for (Document document : documentStream) {
                AlbumRate rate = mapOjaiDocument(document);
                if (rate != null) {
                    rates.add(rate);
                }
            }

            return rates;
        });
    }

    @Override
    public AlbumRate update(String id, AlbumRate albumRate) {
        return processStore((connection, store) -> {

            // Create a DocumentMutation to update non-null fields
            DocumentMutation mutation = connection.newMutation();

            // Update only non-null fields
            if (albumRate.getRating() != null) {
                mutation.set("rating", albumRate.getRating());
            }

            // Update the OJAI Document with specified identifier
            store.update(id, mutation);

            Document updatedOjaiDoc = store.findById(id);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }
}
