package com.mapr.music.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.dao.ArtistDao;
import com.mapr.music.model.Artist;
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

public class ArtistDaoImpl implements ArtistDao {

    private static final String ALBUMS_TABLE_PATH = "/apps/artists";

    private static final Logger log = LoggerFactory.getLogger(AlbumDaoImpl.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Artist> getAll(long offset, long limit) {
        return MapRDBUtil.processStore(ALBUMS_TABLE_PATH, (connection, store) -> {

            Query query = connection.newQuery()
                    .select("*")
                    .offset(offset)
                    .limit(limit)
                    .build();

            DocumentStream documentStream = store.findQuery(query);
            List<Artist> artists = new ArrayList<>();
            for (Document albumDocument : documentStream) {
                Artist artist = mapDocument(albumDocument);
                if (artist != null) {
                    artists.add(artist);
                }
            }

            return artists;
        });
    }

    @Override
    public List<Artist> getAll(long offset, long limit, String... fields) {
        return MapRDBUtil.processStore(ALBUMS_TABLE_PATH, (connection, store) -> {

            Query query = connection.newQuery()
                    .select(fields)
                    .offset(offset)
                    .limit(limit)
                    .build();

            DocumentStream documentStream = store.findQuery(query);
            List<Artist> artists = new ArrayList<>();
            for (Document albumDocument : documentStream) {
                Artist artist = mapDocument(albumDocument);
                if (artist != null) {
                    artists.add(artist);
                }
            }

            return artists;
        });
    }

    @Override
    public Artist getById(String id) {
        return MapRDBUtil.processStore(ALBUMS_TABLE_PATH, (connection, store) -> {

            Document albumDocument = store.findById(id);
            if (albumDocument == null) {
                throw new NotFoundException("Artist with id '" + id + "' not found");
            }

            return mapDocument(albumDocument);
        });
    }

    @Override
    public Artist getById(String id, String... fields) {
        return MapRDBUtil.processStore(ALBUMS_TABLE_PATH, (connection, store) -> {

            Document albumDocument = store.findById(id, fields);
            if (albumDocument == null) {
                throw new NotFoundException("Album with id '" + id + "' not found");
            }

            return mapDocument(albumDocument);
        });
    }

    @Override
    public long getTotalNum() {
        return MapRDBUtil.processStore(ALBUMS_TABLE_PATH, (connection, store) -> {

            // FIXME IS THERE "COUNT" METHOD?
            DocumentStream documentStream = store.find();
            long totalNum = 0;
            for (Document ignored : documentStream) {
                totalNum++;
            }

            return totalNum;
        });
    }

    private Artist mapDocument(Document albumDocument) {
        Artist artist;

        try {
            artist = mapper.readValue(albumDocument.toString(), Artist.class);
        } catch (IOException e) {
            log.warn("Can not parse artist document '{}' as instance of '{}' class.", albumDocument,
                    Artist.class.getCanonicalName());

            throw new RuntimeException(e);
        }

        return artist;
    }
}
