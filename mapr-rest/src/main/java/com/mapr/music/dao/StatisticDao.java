package com.mapr.music.dao;

import com.google.common.base.Stopwatch;
import com.mapr.music.model.Statistic;
import org.ojai.Document;
import org.ojai.store.DocumentMutation;

import javax.inject.Named;

@Named("statisticDao")
public class StatisticDao extends MaprDbDao<Statistic> {

    public StatisticDao() {
        super(Statistic.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param id        identifier of document, which will be updated.
     * @param statistic statistic.
     * @return updated statistic.
     */
    @Override
    public Statistic update(String id, Statistic statistic) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Create a DocumentMutation to update non-null fields
            DocumentMutation mutation = connection.newMutation();

            // Update only non-null fields
            if (statistic.getDocumentNumber() != null) {
                mutation.set("document_number", statistic.getDocumentNumber());
            }

            // Update the OJAI Document with specified identifier
            store.update(id, mutation);

            Document updatedOjaiDoc = store.findById(id);

            log.info("Update document from table '{}' with id: '{}'. Elapsed time: {}", tablePath, id, stopwatch);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }

    /**
     * Indicates if statistic table is empty.
     *
     * @return <code>true</code> if statistic table is empty, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return getList(0, 1).isEmpty();
    }
}
