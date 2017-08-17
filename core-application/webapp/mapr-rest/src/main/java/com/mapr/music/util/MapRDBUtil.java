package com.mapr.music.util;

import org.apache.hadoop.security.UserGroupInformation;
import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;
import org.ojai.store.DriverManager;


/**
 * Utility class which is handles OJAI connections to MapR cluster.
 */
public class MapRDBUtil {

    // TODO
    private static final String TEST_USER_NAME = "mapr";
    private static final String TEST_USER_GROUP = "mapr";

    private static final String ALBUMS_TABLE_PATH = "/apps/albums";
    private static final String CONNECTION_URL = "ojai:mapr:";

    public static <T> T processAlbumsStore(MapRDBStoreAction<T> storeAction) {
        return processStore(CONNECTION_URL, ALBUMS_TABLE_PATH, storeAction);
    }

    public static <T> T processStore(String connectionUrl, String tablePath, MapRDBStoreAction<T> storeAction) {

        if (connectionUrl == null || connectionUrl.isEmpty()) {
            throw new IllegalArgumentException("Connection URL can not be empty");
        }

        if (tablePath == null || tablePath.isEmpty()) {
            throw new IllegalArgumentException("Table path can not be empty");
        }

        loginTestUser(TEST_USER_NAME, TEST_USER_GROUP);

        // Create an OJAI connection to MapR cluster
        Connection connection = DriverManager.getConnection(connectionUrl);

        // Get an instance of OJAI DocumentStore
        final DocumentStore store = connection.getStore(tablePath);

        if (storeAction != null) {
            return storeAction.process(connection, store);
        }

        // Close this instance of OJAI DocumentStore
        store.close();

        // Close the OJAI connection and release any resources held by the connection
        connection.close();

        return null;
    }

    // TODO
    private static void loginTestUser(String username, String group) {
        UserGroupInformation currentUgi = UserGroupInformation.createUserForTesting(username, new String[]{group});
        UserGroupInformation.setLoginUser(currentUgi);
    }

}
