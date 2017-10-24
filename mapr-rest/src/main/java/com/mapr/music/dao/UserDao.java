package com.mapr.music.dao;

import com.google.common.base.Stopwatch;
import com.mapr.music.model.User;
import org.ojai.Document;
import org.ojai.store.DocumentMutation;

import javax.inject.Named;

@Named("userDao")
public class UserDao extends MaprDbDao<User> {

    public UserDao() {
        super(User.class);
    }

    @Override
    public User update(String id, User user) {
        return processStore((connection, store) -> {

            Stopwatch stopwatch = Stopwatch.createStarted();

            // Create a DocumentMutation to update non-null fields
            DocumentMutation mutation = connection.newMutation();

            // Update only non-null fields
            if (user.getFirstName() != null) {
                mutation.set("first_name", user.getFirstName());
            }

            if (user.getLastName() != null) {
                mutation.set("last_name", user.getLastName());
            }

            // Update the OJAI Document with specified identifier
            store.update(id, mutation);

            Document updatedOjaiDoc = store.findById(id);

            log.debug("Update document from table '{}' with id: '{}'. Elapsed time: {}", tablePath, id, stopwatch);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }
}
