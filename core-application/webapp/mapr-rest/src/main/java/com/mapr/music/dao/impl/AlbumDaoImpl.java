package com.mapr.music.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.dao.AlbumDao;
import com.mapr.music.model.Album;
import com.mapr.music.util.MapRDBUtil;
import org.ojai.Document;
import org.ojai.DocumentStream;
import org.ojai.store.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Actual implementation of {@link AlbumDao} which is queries MapR DB using OJAI API Library.
 */
public class AlbumDaoImpl implements AlbumDao {

    private static final Logger log = LoggerFactory.getLogger(AlbumDaoImpl.class);

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * {@inheritDoc}
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @return list of albums.
     */
    @Override
    public List<Album> getAlbumList(final long offset, final long limit) {

        return MapRDBUtil.processAlbumsStore((connection, store) -> {

            // Build an OJAI query with an offset and limit
            Query query = connection.newQuery()
                    .select("*")
                    .offset(offset)
                    .limit(limit)
                    .build();

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            List<Album> albums = new ArrayList<>();
            for (Document albumDocument : documentStream) {
                Album album = mapDocumentToAlbum(albumDocument);
                if (album != null) {
                    albums.add(album);
                }
            }

            return albums;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param offset offset value.
     * @param limit  limit value.
     * @param fields list of fields that will be returned.
     * @return list of albums.
     */
    @Override
    public List<Album> getAlbumList(long offset, long limit, String... fields) {
        return MapRDBUtil.processAlbumsStore((connection, store) -> {

            // Build an OJAI query with an offset and limit
            Query query = connection.newQuery()
                    .select(fields) // only specified fields will be fetched because of projection
                    .offset(offset)
                    .limit(limit)
                    .build();

            // Fetch all OJAI Documents from this store according to the built query
            DocumentStream documentStream = store.findQuery(query);
            List<Album> albums = new ArrayList<>();
            for (Document albumDocument : documentStream) {
                Album album = mapDocumentToAlbum(albumDocument);
                if (album != null) {
                    albums.add(album);
                }
            }

            return albums;
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    @Override
    public Album getById(String id) {

        return MapRDBUtil.processAlbumsStore((connection, store) -> {

            // Fetch single OJAI Document from Albums store by it's identifier
            Document albumDocument = store.findById(id);
            if (albumDocument == null) {
                throw new NotFoundException("Album with id '" + id + "' not found");
            }

            return mapDocumentToAlbum(albumDocument);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param id     album's identifier.
     * @param fields list of fields that will present in album.
     * @return album with the specified identifier.
     */
    @Override
    public Album getById(String id, String... fields) {
        return MapRDBUtil.processAlbumsStore((connection, store) -> {

            // Fetch single OJAI Document from Albums store by it's identifier
            Document albumDocument = store.findById(id, fields);
            if (albumDocument == null) {
                throw new NotFoundException("Album with id '" + id + "' not found");
            }

            return mapDocumentToAlbum(albumDocument);
        });
    }

    /**
     * {@inheritDoc}
     *
     * @return total number of albums.
     */
    @Override
    public long getTotalNum() {
        return MapRDBUtil.processAlbumsStore((connection, store) -> {

            // TODO is there "count" method
            DocumentStream documentStream = store.find();
            long totalNum = 0;
            for (Document ignored : documentStream) {
                totalNum++;
            }

            return totalNum;
        });
    }

    private Album mapDocumentToAlbum(Document albumDocument) {

        Album album = null;
        try {
            album = mapper.readValue(albumDocument.toString(), com.mapr.music.model.Album.class);
        } catch (IOException e) {
            log.warn("Can not parse album document '{}' as instance of '{}' class.", albumDocument,
                    com.mapr.music.model.Album.class.getCanonicalName());
        }

        return album;
    }
}
