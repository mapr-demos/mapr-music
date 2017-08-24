package com.mapr.music.service.impl;

import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.SortOption;
import com.mapr.music.dao.impl.AlbumDao;
import com.mapr.music.dao.impl.ArtistsDao;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.PaginatedService;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Actual implementation of {@link AlbumService} which is responsible of performing all business logic.
 */
@Named
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

    private final MaprDbDao<Album> albumDao;
    private final MaprDbDao<Artist> artistDao;

    public AlbumServiceImpl() {
        this.albumDao = new AlbumDao();
        this.artistDao = new ArtistsDao();
    }

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
    public ResourceDto<AlbumDto> getAlbumsPage() {
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
    public ResourceDto<AlbumDto> getAlbumsPage(String order, List<String> orderFields) {
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
    public ResourceDto<AlbumDto> getAlbumsPage(Long page) {
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
    public ResourceDto<AlbumDto> getAlbumsPage(Long perPage, Long page, String order, List<String> orderFields) {

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

        ResourceDto<AlbumDto> albumsPage = new ResourceDto<>();
        albumsPage.setPagination(getPaginationInfo(page, perPage));
        long offset = (page - 1) * perPage;

        SortOption[] sortOptions = null;
        if (order != null && orderFields != null && orderFields.size() > 0) {
            SortOption.Order sortOrder = SortOption.Order.valueOf(order.toUpperCase());
            sortOptions = new SortOption[]{new SortOption(sortOrder, orderFields)};
        }

        List<Album> albums = albumDao.getList(offset, perPage, sortOptions, ALBUM_SHORT_INFO_FIELDS);
        List<AlbumDto> albumDtoList = albums.stream()
                .map(this::albumToDto)
                .collect(Collectors.toList());

        albumsPage.setResults(albumDtoList);

        return albumsPage;
    }

    /**
     * {@inheritDoc}
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    @Override
    public AlbumDto getAlbumById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        return albumToDto(albumDao.getById(id));
    }

    /**
     * {@inheritDoc}
     *
     * @param id identifier of album which will be deleted.
     */
    @Override
    public void deleteAlbumById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        albumDao.deleteById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @param album contains album info.
     * @return created album.
     */
    @Override
    public AlbumDto createAlbum(Album album) {

        if (album == null) {
            throw new IllegalArgumentException("Album can not be null");
        }

        String id = UUID.randomUUID().toString();
        album.setId(id);

        return albumToDto(albumDao.create(album));
    }

    /**
     * {@inheritDoc}
     *
     * @param album album which will be updated. Note, that album's id must be set, otherwise
     *              {@link IllegalArgumentException} will be thrown.
     * @return updated album.
     */
    @Override
    public AlbumDto updateAlbum(Album album) {

        if (album == null || album.getId() == null || album.getId().isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        return albumToDto(albumDao.update(album.getId(), album));
    }

    /**
     * {@inheritDoc}
     *
     * @param id    identifier of album which will be updated.
     * @param album album which will be updated.
     * @return updated album.
     */
    @Override
    public AlbumDto updateAlbum(String id, Album album) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (album == null) {
            throw new IllegalArgumentException("Album can not be null");
        }

        return albumToDto(albumDao.update(id, album));
    }

    private AlbumDto albumToDto(Album album) {
        AlbumDto albumDto = new AlbumDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(albumDto, album);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create album Data Transfer Object", e);
        }

        return albumDto;
    }

}
