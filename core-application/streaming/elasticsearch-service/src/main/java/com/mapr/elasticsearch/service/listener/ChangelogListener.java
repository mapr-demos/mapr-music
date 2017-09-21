package com.mapr.elasticsearch.service.listener;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.Consumer;

public interface ChangelogListener {

    interface ChangeDataRecordCallback {
        void callback(String documentId, JsonNode changes);
    }

    void onInsert(ChangeDataRecordCallback callback);

    void onUpdate(ChangeDataRecordCallback callback);

    void onDelete(Consumer<String> callback);

    void listen();
}
