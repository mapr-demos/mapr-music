package com.mapr.music.service.impl;

import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.MaprDbDaoImpl;
import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.PaginatedService;

import java.util.List;

/**
 * Actual implementation of {@link AlbumService} which is responsible of performing all business logic.
 */
public class AlbumServiceImpl implements AlbumService, PaginatedService {

    private static final long ALBUMS_PER_PAGE_DEFAULT = 50;
    private static final long FIRST_PAGE_NUM = 1;

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
    private MaprDbDao<Album> albumDao = new MaprDbDaoImpl<>(Album.class);

    @Override
    public long getTotalNum() {
        return albumDao.getTotalNum();
    }

    /**
     * {@inheritDoc}
     *
     * @return first albums page resource.
     */
    @Override
    public ResourceDto<Album> getAlbumsPage() {
        return getAlbumsPage(FIRST_PAGE_NUM);
    }

    /**
     * {@inheritDoc}
     *
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<Album> getAlbumsPage(String order, List<String> orderFields) {
        return getAlbumsPage(ALBUMS_PER_PAGE_DEFAULT, FIRST_PAGE_NUM, order, orderFields);
    }

    /**
     * {@inheritDoc}
     *
     * @param page specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *             first page will be returned.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<Album> getAlbumsPage(Long page) {
        return getAlbumsPage(ALBUMS_PER_PAGE_DEFAULT, page, null, null);
    }

    /**
     * {@inheritDoc}
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value {@link AlbumServiceImpl#ALBUMS_PER_PAGE_DEFAULT} will be used.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *                    first page will be returned.
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<Album> getAlbumsPage(Long perPage, Long page, String order, List<String> orderFields) {

        if (page == null) {
            page = FIRST_PAGE_NUM;
        }

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        if (perPage == null) {
            perPage = ALBUMS_PER_PAGE_DEFAULT;
        }

        if (perPage <= 0) {
            throw new IllegalArgumentException("Per page value must be greater than zero");
        }

        ResourceDto<Album> albumsPage = new ResourceDto<>();
        albumsPage.setPagination(getPaginationInfo(page, perPage));
        long offset = (page - 1) * perPage;

        SortOption[] sortOptions = null;
        if (order != null && orderFields != null && orderFields.size() > 0) {
            SortOption.Order sortOrder = SortOption.Order.valueOf(order.toUpperCase());
            sortOptions = new SortOption[]{new SortOption(sortOrder, orderFields)};
        }

        List<Album> albums = albumDao.getList(offset, perPage, sortOptions, ALBUM_SHORT_INFO_FIELDS);
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
