package com.mapr.music.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.mapr.music.annotation.MaprDbTable;
import org.apache.hadoop.security.UserGroupInformation;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

import static com.mapr.music.util.MaprProperties.MAPR_USER_GROUP;
import static com.mapr.music.util.MaprProperties.MAPR_USER_NAME;

/**
 * Implements common methods to access MapR-DB using OJAI driver.
 *
 * @param <T> model type.
 */
public abstract class MaprDbDao<T> {

    public interface OjaiStoreAction<R> {
        R process(Connection connection, DocumentStore store);
    }

    public interface OjaiStoreVoidAction {
        void process(Connection connection, DocumentStore store);
    }

    protected static final String CONNECTION_URL = "ojai:mapr:";

    protected static final Logger log = LoggerFactory.getLogger(MaprDbDao.class);

    protected final ObjectMapper mapper = new ObjectMapper();
    protected final Class<T> documentClass;
    protected String tablePath;

    public MaprDbDao(Class<T> documentClass) {

        MaprDbTable tableAnnotation = documentClass.getAnnotation(MaprDbTable.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Document class must be annotated with '" +
                    MaprDbTable.class.getCanonicalName() + " annotation.");
        }

        this.tablePath = tableAnnotation.value();
        this.documentClass = documentClass;
    }

    /**
     * Returns list of all documents.
     *
     * @return list of documents.
     */
    public List<T> getList() {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Fetch all OJAI Documents from this store
            DocumentStream documentStream = store.find();
            List<T> documents = new ArrayList<>();
            for (Document document : documentStream) {
                T doc = mapOjaiDocument(document);
                if (doc != null) {
                    documents.add(doc);
                }
            }

            log.debug("Get list of '{}' documents from '{}' table. Elapsed time: {}", documents.size(), tablePath,
                    stopwatch);

            return documents;
        });
    }

    /**
     * Returns list of documents according to specified <code>offset</code> and <code>limit</code> values.
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @return list of documents.
     */
    public List<T> getList(long offset, long limit) {
        return getList(offset, limit, null, new String[]{});
    }

    /**
     * Returns list of document according to specified <code>offset</code> and <code>limit</code> values using
     * projection. Documents will be ordered according to the specified {@link SortOption} options.
     *
     * @param offset      offset value.
     * @param limit       limit value.
     * @param sortOptions define the order of documents.
     * @return list of documents.
     */
    public List<T> getList(long offset, long limit, SortOption... sortOptions) {
        return getList(offset, limit, Arrays.asList(sortOptions));
    }

    /**
     * Returns list of document according to specified <code>offset</code> and <code>limit</code> values using
     * projection.
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @param fields list of fields that will present in document.
     * @return list of documents.
     */
    public List<T> getList(long offset, long limit, String... fields) {
        return getList(offset, limit, null, fields);
    }

    /**
     * Returns list of document according to specified <code>offset</code> and <code>limit</code> values using
     * projection. Documents will be ordered according to the specified {@link SortOption} options.
     *
     * @param offset      offset value.
     * @param limit       limit value.
     * @param sortOptions define the order of documents.
     * @param fields      list of fields that will present in document.
     * @return list of documents.
     */
    public List<T> getList(long offset, long limit, List<SortOption> sortOptions, String... fields) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

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

            log.debug("Get list of '{}' documents from '{}' table with offset: '{}', limit: '{}', sortOptions: '{}', " +
                            "fields: '{}'. Elapsed time: {}", documents.size(), tablePath, offset, limit, sortOptions,
                    (fields != null) ? Arrays.asList(fields) : "[]", stopwatch);

            return documents;
        });
    }

    /**
     * Returns single document by it's identifier. If there is no such document <code>null</code> will be returned.
     *
     * @param id document's identifier.
     * @return document with the specified identifier.
     */
    public T getById(String id) {
        return getById(id, new String[]{});
    }

    /**
     * Returns single document by it's identifier using projection. Note that only specified fields will be filled with
     * values. If there is no such document <code>null</code> will be returned.
     *
     * @param id     document's identifier.
     * @param fields list of fields that will present in document.
     * @return document with the specified identifier.
     */
    public T getById(String id, String... fields) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Fetch single OJAI Document from store by it's identifier. Use projection if fields are defined.
            Document ojaiDoc = (fields == null || fields.length == 0) ? store.findById(id) : store.findById(id, fields);

            log.debug("Get by ID from '{}' table with id: '{}', fields: '{}'. Elapsed time: {}", tablePath, id,
                    (fields != null) ? Arrays.asList(fields) : "[]", stopwatch);

            return (ojaiDoc == null) ? null : mapOjaiDocument(ojaiDoc);
        });
    }

    /**
     * Allows to specify action via {@link OjaiStoreAction} to access the OJAI store.
     *
     * @param storeAction specifies action which will be performed on store.
     * @param <R>         type of {@link OjaiStoreAction} return value.
     * @return process result.
     */
    public <R> R processStore(OjaiStoreAction<R> storeAction) {

        loginTestUser(MAPR_USER_NAME, MAPR_USER_GROUP);

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
     * Allows to specify action via {@link OjaiStoreVoidAction} to access the OJAI store. Note, that no result will be
     * returned.
     *
     * @param storeVoidAction specifies action which will be performed on store.
     */
    public void processStore(OjaiStoreVoidAction storeVoidAction) {

        OjaiStoreAction<Optional> storeAction = (connection, store) -> {
            storeVoidAction.process(connection, store);
            return Optional.empty();
        };

        processStore(storeAction);
    }

    /**
     * Deletes single document by it's identifier.
     *
     * @param id identifier of document which will be deleted.
     */
    public void deleteById(String id) {
        processStore((connection, store) -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            store.delete(id);
            log.debug("Delete by ID from '{}' table with id: '{}'. Elapsed time: {}", tablePath, id, stopwatch);
        });
    }

    /**
     * Creates single document.
     *
     * @param entity contains info for document, which will be created.
     * @return created document.
     */
    public T create(T entity) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Create an OJAI Document form the Java bean (there are other ways too)
            final Document createdOjaiDoc = connection.newDocument(entity);

            // Set update info if available
            getUpdateInfo().ifPresent(updateInfo -> createdOjaiDoc.set("update_info", updateInfo));

            // Insert the document into the OJAI store
            store.insertOrReplace(createdOjaiDoc);

            log.debug("Create document '{}' at table: '{}'. Elapsed time: {}", createdOjaiDoc, tablePath, stopwatch);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(createdOjaiDoc);
        });
    }

    /**
     * Updates single document.
     *
     * @param id     identifier of document, which will be updated.
     * @param entity contains info for document, which will be updated.
     * @return updated document.
     */
    public abstract T update(String id, T entity);

    /**
     * Indicates whether document with specified identifier exists.
     *
     * @param id document's identifier.
     * @return <code>true</code> if document with specified identifier exists, <code>false</code> otherwise.
     */
    public boolean exists(String id) {
        return processStore((connection, store) -> store.findById(id) != null);
    }

    /**
     * Converts OJAI document to the instance of model class.
     *
     * @param ojaiDocument OJAI document which will be converted.
     * @return instance of the model class.
     */
    public T mapOjaiDocument(Document ojaiDocument) {

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
     * Constructs and returns map, which contains document update information.
     *
     * @return map, which contains document update information.
     */
    protected Optional<Map<String, Object>> getUpdateInfo() {

        Principal principal = ResteasyProviderFactory.getContextData(Principal.class);
        if (principal == null) {
            return Optional.empty();
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user_id", principal.getName());
        userInfo.put("date_of_operation", System.currentTimeMillis());

        return Optional.of(userInfo);
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
    private Query buildQuery(Connection connection, long offset, long limit, String[] fields, List<SortOption> options) {

        Query query = connection.newQuery();
        if (fields == null || fields.length == 0) {
            query.select("*");
        } else {
            query.select(fields);
        }

        if (options == null || options.isEmpty()) {
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

    private static void loginTestUser(String username, String group) {
        UserGroupInformation currentUgi = UserGroupInformation.createUserForTesting(username, new String[]{group});
        UserGroupInformation.setLoginUser(currentUgi);
    }
}
