package com.mapr.music.dao;

import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;

import java.util.List;

public interface MaprDbDao<T> {

    interface OjaiStoreAction<R> {
        R process(Connection connection, DocumentStore store);
    }

    /**
     * Returns list of documents according to specified <code>offset</code> and <code>limit</code> values.
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @return list of document.
     */
    List<T> getList(long offset, long limit);

    /**
     * Returns list of document according to specified <code>offset</code> and <code>limit</code> values using
     * projection. Documents will be ordered according to the specified {@link SortOption} options.
     *
     * @param offset      offset value.
     * @param limit       limit value.
     * @param sortOptions define the order of documents.
     * @return list of document.
     */
    List<T> getList(long offset, long limit, SortOption... sortOptions);

    /**
     * Returns list of document according to specified <code>offset</code> and <code>limit</code> values using
     * projection.
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @param fields list of fields that will present in document.
     * @return list of document.
     */
    List<T> getList(long offset, long limit, String... fields);

    /**
     * Returns list of document according to specified <code>offset</code> and <code>limit</code> values using
     * projection. Documents will be ordered according to the specified {@link SortOption} options.
     *
     * @param offset      offset value.
     * @param limit       limit value.
     * @param sortOptions define the order of documents.
     * @param fields      list of fields that will present in document.
     * @return list of document.
     */
    List<T> getList(long offset, long limit, SortOption[] sortOptions, String... fields);

    /**
     * Returns single document by it's identifier.
     *
     * @param id document's identifier.
     * @return document with the specified identifier.
     */
    T getById(String id);

    /**
     * Returns single document by it's identifier using projection. Note that only specified fields will be filled with
     * values.
     *
     * @param id     document's identifier.
     * @param fields list of fields that will present in document.
     * @return document with the specified identifier.
     */
    T getById(String id, String... fields);

    /**
     * Counts total number of documents.
     *
     * @return total number of documents.
     */
    long getTotalNum();

    /**
     * Allows to specify action via {@link OjaiStoreAction} to access the OJAI store.
     *
     * @param storeAction specifies action which will be performed on store.
     * @param <R>         type of {@link OjaiStoreAction} return value.
     * @return
     */
    <R> R processStore(OjaiStoreAction<R> storeAction);

}
