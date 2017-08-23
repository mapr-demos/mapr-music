package com.mapr.music.dao.impl;

import com.mapr.music.model.Artist;
import org.ojai.Document;
import org.ojai.store.DocumentMutation;

public class ArtistsDao extends MaprDbDaoImpl<Artist> {

    public ArtistsDao() {
        super(Artist.class);
    }

    @Override
    public Artist update(String id, Artist artist) {
        return processStore((connection, store) -> {

            // Create a DocumentMutation to update the zipCode field
            DocumentMutation mutation = connection.newMutation();

            // FIXME get rid of hardcode
            if (artist.getName() != null) {
                mutation.set("name", artist.getName());
            }
            if (artist.getGender() != null) {
                mutation.set("gender", artist.getGender());
            }
            if (artist.getArea() != null) {
                mutation.set("area", artist.getArea());
            }

            // Update the OJAI Document with specified identifier
            store.update(id, mutation);

            Document updatedOjaiDoc = store.findById(id);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }
}
