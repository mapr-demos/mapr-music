package com.mapr.music.util;

import org.ojai.store.Connection;
import org.ojai.store.DocumentStore;

public interface MapRDBStoreAction<T> {

    T process(Connection connection, DocumentStore store);
}
