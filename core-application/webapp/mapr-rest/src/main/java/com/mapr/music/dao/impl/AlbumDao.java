package com.mapr.music.dao.impl;

import com.mapr.music.model.Album;
import org.ojai.Document;
import org.ojai.store.DocumentMutation;

public class AlbumDao extends MaprDbDaoImpl<Album> {

    public AlbumDao() {
        super(Album.class);
    }

    @Override
    public Album update(String id, Album album) {
        return processStore((connection, store) -> {

            // Create a DocumentMutation to update the zipCode field
            DocumentMutation mutation = connection.newMutation();

            // FIXME get rid of hardcode
            if (album.getName() != null) {
                mutation.set("name", album.getName());
            }
            if (album.getGenre() != null) {
                mutation.set("genre", album.getGenre());
            }
            if (album.getStyle() != null) {
                mutation.set("style", album.getStyle());
            }
            if (album.getFormat() != null) {
                mutation.set("format", album.getFormat());
            }
            if (album.getCountry() != null) {
                mutation.set("country", album.getCountry());
            }
            if (album.getBarcode() != null) {
                mutation.set("barcode", album.getBarcode());
            }

            // Update the OJAI Document with specified identifier
            store.update(id, mutation);

            Document updatedOjaiDoc = store.findById(id);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(updatedOjaiDoc);
        });
    }
}
