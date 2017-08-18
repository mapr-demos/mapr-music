package com.mapr.music.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.annotation.MaprDbTable;
import org.apache.hadoop.security.UserGroupInformation;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MaprDbDaoImpl<T> implements MaprDbDao<T> {

    // TODO
    protected static final String TEST_USER_NAME = "mapr";
    protected static final String TEST_USER_GROUP = "mapr";
    protected static final String CONNECTION_URL = "ojai:mapr:";

    protected static final Logger log = LoggerFactory.getLogger(MaprDbDaoImpl.class);

    protected final ObjectMapper mapper = new ObjectMapper();
    protected final Class<T> documentClass;
    protected String tablePath;

    public MaprDbDaoImpl(Class<T> documentClass) {

        MaprDbTable tableAnnotation = documentClass.getAnnotation(MaprDbTable.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Document class must be annotated with '" +
                    MaprDbTable.class.getCanonicalName() + " annotation.");
        }

        this.tablePath = tableAnnotation.value();
        this.documentClass = documentClass;
    }

    /**
     * {@inheritDoc}
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @return list of document.
     */
    @Override
    public List<T> getList(long offset, long limit) {
        return getList(offset, limit, null, new String[]{});
    }

    /**
     * {@inheritDoc}
     *
     * @param offset      offset value.
     * @param limit       limit value.
     * @param sortOptions define the order of documents.
     * @return list of document.
     */
    @Override
    public List<T> getList(long offset, long limit, SortOption... sortOptions) {
        return getList(offset, limit, sortOptions, new String[]{});
    }

    /**
     * {@inheritDoc}
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @param fields list of fields that will present in document.
     * @return list of document.
     */
    @Override
    public List<T> getList(long offset, long limit, String... fields) {
        return getList(offset, limit, null, fields);
    }

    /**
     * {@inheritDoc}
     *
     * @param offset      offset value.
     * @param limit       limit value.
     * @param sortOptions define the order of documents.
     * @param fields      list of fields that will present in document.
     * @return list of document.
     */
    @Override
    public List<T> getList(long offset, long limit, SortOption[] sortOptions, String... fields) {
        return processStore((connection, store) -> {

            Query query = buildQuery(connection, offset, limit, fields, sortOptions);

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            List<T> documents = new ArrayList<>();
            for (Document document : documentStream) {
                T doc = mapOjaiDocument(document);
                if (doc != null) {
                    documents.add(doc);
                }
            }

            return documents;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param id document's identifier.
     * @return document with the specified identifier.
     */
    @Override
    public T getById(String id) {
        return getById(id, new String[]{});
    }

    /**
     * {@inheritDoc}
     *
     * @param id     document's identifier.
     * @param fields list of fields that will present in document.
     * @return document with the specified identifier.
     */
    @Override
    public T getById(String id, String... fields) {
        return processStore((connection, store) -> {

            // Fetch single OJAI Document from store by it's identifier. Use projection if fields are defined.
            Document ojaiDoc = (fields == null || fields.length == 0) ? store.findById(id) : store.findById(id, fields);
            if (ojaiDoc == null) {
                throw new NotFoundException("Document with id '" + id + "' not found");
            }

            return mapOjaiDocument(ojaiDoc);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @return total number of documents.
     */
    @Override
    public long getTotalNum() {
        return processStore((connection, store) -> {

            // TODO is there "count" method
            DocumentStream documentStream = store.find();
            long totalNum = 0;
            for (Document ignored : documentStream) {
                totalNum++;
            }

            return totalNum;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param storeAction specifies action which will be performed on store.
     * @param <R>         type of {@link OjaiStoreAction} return value.
     * @return
     */
    @Override
    public <R> R processStore(OjaiStoreAction<R> storeAction) {

        loginTestUser(TEST_USER_NAME, TEST_USER_GROUP);

        // Create an OJAI connection to MapR cluster
        Connection connection = DriverManager.getConnection(CONNECTION_URL);

        // Get an instance of OJAI DocumentStore
        final DocumentStore store = connection.getStore(tablePath);

        R processingResult = null;
        if (storeAction != null) {
            processingResult = storeAction.process(connection, store);
        }

        // Close this instance of OJAI DocumentStore
        store.close();

        // Close the OJAI connection and release any resources held by the connection
        connection.close();

        return processingResult;
    }

    /**
     * Converts OJAI document to the instance of model class.
     *
     * @param ojaiDocument OJAI document which will be converted.
     * @return instance of the model class.
     */
    protected T mapOjaiDocument(Document ojaiDocument) {

        if (ojaiDocument == null) {
            throw new IllegalArgumentException("OJAI document can not be null");
        }

        T document = null;
        try {
            document = mapper.readValue(ojaiDocument.toString(), documentClass);
        } catch (IOException e) {
            log.warn("Can not map OJAI document '{}' to instance of '{}' class. Exception: {}", ojaiDocument,
                    documentClass.getCanonicalName(), e);
        }

        return document;
    }

    /**
     * Build an OJAI query according to the specified offset and limit values. Given fields array will be used in
     * projection.
     *
     * @param connection OJAI connection.
     * @param offset     offset value.
     * @param limit      limit value.
     * @param fields     fields what will present in returned document. Used for projection.
     * @return OJAI query, which is built according to the specified parameters.
     */
    private Query buildQuery(Connection connection, long offset, long limit, String[] fields, SortOption[] options) {

        Query query = connection.newQuery();
        if (fields == null || fields.length == 0) {
            query.select("*");
        } else {
            query.select(fields);
        }

        if (options == null || options.length == 0) {
            return query.offset(offset).limit(limit).build();
        }

        for (SortOption sortOption : options) {
            SortOrder ojaiSortOrder = (SortOption.Order.DESC == sortOption.getOrder()) ? SortOrder.DESC : SortOrder.ASC;
            for (String field : sortOption.getFields()) {
                query = query.orderBy(field, ojaiSortOrder);
            }
        }

        return query.offset(offset).limit(limit).build();
    }

    // TODO
    private static void loginTestUser(String username, String group) {
        UserGroupInformation currentUgi = UserGroupInformation.createUserForTesting(username, new String[]{group});
        UserGroupInformation.setLoginUser(currentUgi);
    }
}
