package com.mapr.music.dao.impl;

import com.mapr.music.model.Album;
import org.ojai.Document;
import org.ojai.store.DocumentMutation;

/**
 * Actual implementation of {@link com.mapr.music.dao.MaprDbDao} to manage {@link Album} model.
 */
public class AlbumDao extends MaprDbDaoImpl<Album> {

    public AlbumDao() {
        super(Album.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param id    identifier of album, which will be updated.
     * @param album contains album info that will be updated.
     * @return updated album.
     */
    @Override
    public Album update(String id, Album album) {
        return processStore((connection, store) -> {

            // Create a DocumentMutation to update the zipCode field
            DocumentMutation mutation = connection.newMutation();

            // Update only basic fields
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
