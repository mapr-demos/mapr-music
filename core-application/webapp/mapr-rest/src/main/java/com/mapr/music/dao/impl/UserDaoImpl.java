package com.mapr.music.dao.impl;

import com.mapr.music.model.User;
import org.ojai.Document;
import org.ojai.store.DocumentMutation;

import javax.inject.Named;

@Named("userDao")
public class UserDaoImpl extends MaprDbDaoImpl<User> {

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public User update(String id, User user) {
        return processStore((connection, store) -> {

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

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }
}
