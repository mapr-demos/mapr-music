package com.mapr.music.service.impl;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.impl.AlbumDaoImpl;
import com.mapr.music.dto.Pagination;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.service.AlbumService;

import java.util.List;

/**
 * Actual implementation of {@link AlbumService} which is responsible of performing all business logic.
 */
public class AlbumServiceImpl implements AlbumService {

    private static final long ALBUMS_PER_PAGE_DEFAULT = 50;

    /**
     * Array of album's fields that will be used for projection.
     */
    private static final String[] ALBUM_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "genre",
            "style",
            "barcode",
            "format",
            "country",
            "catalog_numbers",
            "released_date",
            "cover_image_url",
            "artist_list"
    };

    // FIXME use DI
    private AlbumDao albumDao = new AlbumDaoImpl();

    /**
     * {@inheritDoc}
     *
     * @return albums page resource.
     */
    @Override
    public ResourceDto<Album> getAlbumsPage() {
        return getAlbumsPage(1);
    }

    /**
     * {@inheritDoc}
     *
     * @param page specifies number of page, which will be returned.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<Album> getAlbumsPage(long page) {

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        ResourceDto<Album> albumsPage = new ResourceDto<>();

        long totalNum = albumDao.getTotalNum();
        long remainder = totalNum % ALBUMS_PER_PAGE_DEFAULT;
        long pages = (remainder == 0) ? totalNum / ALBUMS_PER_PAGE_DEFAULT : totalNum / ALBUMS_PER_PAGE_DEFAULT + 1;
        albumsPage.setPagination(new Pagination(ALBUMS_PER_PAGE_DEFAULT, totalNum, page, pages));

        long offset = (page - 1) * ALBUMS_PER_PAGE_DEFAULT;
        List<Album> albums = albumDao.getAlbumList(offset, ALBUMS_PER_PAGE_DEFAULT, ALBUM_SHORT_INFO_FIELDS);
        albumsPage.setResults(albums);

        return albumsPage;
    }

    /**
     * {@inheritDoc}
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    @Override
    public Album getById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        return albumDao.getById(id);
    }
}
