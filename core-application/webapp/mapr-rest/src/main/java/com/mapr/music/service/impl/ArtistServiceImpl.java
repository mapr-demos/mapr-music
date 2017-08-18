package com.mapr.music.service.impl;

import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.MaprDbDaoImpl;
import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.service.ArtistService;
import com.mapr.music.service.PaginatedService;

import java.util.Collection;
import java.util.List;

public class ArtistServiceImpl implements ArtistService, PaginatedService {

    private static final long ARTISTS_PER_PAGE_DEFAULT = 50;

    private static final String[] ARTIST_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "gender",
            "area",
            "IPI",
            "ISNI",
            "MBID",
            "disambiguation_comment",
            "release_ids",
            "profile_image_url",
            "images_urls",
            "begin_date",
            "end_date"
    };

    private MaprDbDao<Artist> artistDao = new MaprDbDaoImpl<>(Artist.class);
    private MaprDbDao<Album> albumDao = new MaprDbDaoImpl<>(Album.class);

    @Override
    public long getTotalNum() {
        return artistDao.getTotalNum();
    }

    @Override
    public ResourceDto<Artist> getArtistsPage() {
        return getArtistsPage(1);
    }

    @Override
    public ResourceDto<Artist> getArtistsPage(long page) {
        return getArtistsPage(page, null, null);
    }

    @Override
    public ResourceDto<Artist> getArtistsPage(String order, List<String> orderFields) {
        return getArtistsPage(1, order, orderFields);
    }

    @Override
    public ResourceDto<Artist> getArtistsPage(long page, String order, List<String> orderFields) {

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        ResourceDto<Artist> artistsPage = new ResourceDto<>();
        artistsPage.setPagination(getPaginationInfo(page, ARTISTS_PER_PAGE_DEFAULT));
        long offset = (page - 1) * ARTISTS_PER_PAGE_DEFAULT;

        SortOption[] sortOptions = null;
        if (order != null && orderFields != null && orderFields.size() > 0) {
            SortOption.Order sortOrder = SortOption.Order.valueOf(order.toUpperCase());
            sortOptions = new SortOption[]{new SortOption(sortOrder, orderFields)};
        }

        List<Artist> artists = artistDao.getList(offset, ARTISTS_PER_PAGE_DEFAULT, sortOptions, ARTIST_SHORT_INFO_FIELDS);
        artistsPage.setResults(artists);

        return artistsPage;
    }


    public Artist getById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        return artistDao.getById(id);
    }

    public Artist getById(String id, String... fields) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException("Projection fields can not be null");
        }

        return artistDao.getById(id, fields);
    }

    @Override
    public Artist getById(String id, Collection<String> fields) {

        int fieldsNum = fields.size();
        String[] fieldsArray = fields.toArray(new String[fieldsNum]);

        return getById(id, fieldsArray);
    }
}
