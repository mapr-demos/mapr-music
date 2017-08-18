package com.mapr.music.service.impl;

import com.mapr.music.dao.ArtistDao;
import com.mapr.music.dao.impl.ArtistDaoImpl;
import com.mapr.music.dto.Pagination;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Artist;
import com.mapr.music.service.ArtistService;

import java.util.Collection;
import java.util.List;

public class ArtistServiceImpl implements ArtistService {

    private static final long ARTISTS_PER_PAGE_DEFAULT = 50;

    private static final String[] ARTIST_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "gender",
            "area",
            "ipi",
            "isni",
            "mbid",
            "disambiguation_comment",
            "release_ids",
            "profile_image_url",
            "images_urls",
            "begin_date",
            "end_date"
    };

    private ArtistDao artistDao = new ArtistDaoImpl();

    @Override
    public ResourceDto<Artist> getAll() {
        return getAll(1);
    }

    @Override
    public ResourceDto<Artist> getAll(long page) {

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        ResourceDto<Artist> artistsPage = new ResourceDto<>();

        // TODO MOVE IT TO UTILITY METHOD
        long totalNum = artistDao.getTotalNum();
        long remainder = totalNum % ARTISTS_PER_PAGE_DEFAULT;
        long pages = (remainder == 0) ? totalNum / ARTISTS_PER_PAGE_DEFAULT : totalNum / ARTISTS_PER_PAGE_DEFAULT + 1;
        artistsPage.setPagination(new Pagination(ARTISTS_PER_PAGE_DEFAULT, totalNum, page, pages));

        long offset = (page - 1) * ARTISTS_PER_PAGE_DEFAULT;
        // TODO CHECKING FOR TOTAL NUM
        List<Artist> artists = artistDao.getAll(offset, ARTISTS_PER_PAGE_DEFAULT, ARTIST_SHORT_INFO_FIELDS);
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
