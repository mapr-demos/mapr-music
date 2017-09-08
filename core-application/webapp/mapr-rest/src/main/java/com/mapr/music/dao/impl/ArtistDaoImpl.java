package com.mapr.music.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mapr.music.dao.ArtistDao;
import com.mapr.music.model.Artist;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.DocumentMutation;
import org.ojai.store.Query;
import org.ojai.store.QueryCondition;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Actual implementation of {@link com.mapr.music.dao.MaprDbDao} to manage {@link Artist} model.
 */
@Named("artistDao")
public class ArtistDaoImpl extends MaprDbDaoImpl<Artist> implements ArtistDao {

    private final ObjectMapper mapper = new ObjectMapper();

    public ArtistDaoImpl() {
        super(Artist.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param id     identifier of document, which will be updated.
     * @param artist contains artist info that will be updated.
     * @return updated artist.
     */
    @Override
    public Artist update(String id, Artist artist) {
        return processStore((connection, store) -> {

            // Create a DocumentMutation to update the zipCode field
            DocumentMutation mutation = connection.newMutation();

            // Update only basic fields
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


    /**
     * Creates single artist document. For the sake of example OJAI Document is created form the JSON string. In this
     * case {@link org.ojai.store.Connection#newDocument(String)} method is used.
     * <p>
     * There are also ways to create OJAI documents from the Java beans
     * ({@link org.ojai.store.Connection#newDocument(Object)}) and
     * Maps({@link org.ojai.store.Connection#newDocument(Map)}).
     *
     * @param artist contains artist's info.
     * @return created artist.
     */
    @Override
    public Artist create(Artist artist) {

        return processStore((connection, store) -> {

            // Convert artist instance to JSON string

            ObjectNode artistJsonNode = mapper.valueToTree(artist);

            // Since we creating artist from JSON string we have to specify tags explicitly.
            if (artist.getSlugPostfix() != null) {

                ObjectNode slugPostfix = mapper.createObjectNode();
                slugPostfix.put("$numberLong", artist.getSlugPostfix());
                artistJsonNode.set("slug_postfix", slugPostfix);
            }
            String artistJsonString = artistJsonNode.toString();

            // Create an OJAI Document form the JSON string (there are other ways too)
            final Document createdOjaiDoc = connection.newDocument(artistJsonString);

            // Insert the document into the OJAI store
            store.insertOrReplace(createdOjaiDoc);

            // Map Ojai document to the actual instance of model class
            return mapOjaiDocument(createdOjaiDoc);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param nameEntry specifies query criteria.
     * @param limit     specified limit.
     * @param fields    specifies fields that will be fetched.
     * @return list of artists which names start with the specified name entry.
     */
    @Override
    public List<Artist> getByNameStartsWith(String nameEntry, long limit, String... fields) {
        return processStore((connection, store) -> {

            Query query = connection.newQuery();

            // Select only specified field
            if (fields != null && fields.length > 0) {
                query.select(fields);
            } else {
                query.select("*");
            }

            // Build Query Condition to fetch documents by specified language
            String nameStartsWithPattern = nameEntry + "%";
            QueryCondition nameStartsWithCondition = connection.newCondition()
                    .like("name", nameStartsWithPattern)
                    .build();

            // Add Condition and specified limit to the Query
            query.where(nameStartsWithCondition)
                    .limit(limit)
                    .build();

            DocumentStream documentStream = store.findQuery(query);
            List<Artist> artists = new ArrayList<>();
            for (Document doc : documentStream) {
                artists.add(mapOjaiDocument(doc));
            }

            return artists;
        });
    }
}
