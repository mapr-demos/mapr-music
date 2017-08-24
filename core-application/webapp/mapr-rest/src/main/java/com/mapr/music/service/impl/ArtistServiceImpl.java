package com.mapr.music.service.impl;

import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.SortOption;
import com.mapr.music.dao.impl.AlbumDao;
import com.mapr.music.dao.impl.ArtistsDao;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.service.ArtistService;
import com.mapr.music.service.PaginatedService;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Actual implementation of {@link ArtistService} which is responsible of performing all business logic.
 */
@Named
public class ArtistServiceImpl implements ArtistService, PaginatedService {

    private static final long ARTISTS_PER_PAGE_DEFAULT = 50;
    private static final long FIRST_PAGE_NUM = 1;

    private static final String[] ARTIST_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "gender",
            "area",
            "IPI",
            "ISNI",
            "MBID",
            "disambiguation_comment",
            "albums",
            "profile_image_url",
            "images_urls",
            "begin_date",
            "end_date"
    };

    private final MaprDbDao<Artist> artistDao;
    private final MaprDbDao<Album> albumDao;

    public ArtistServiceImpl() {
        this.artistDao = new ArtistsDao();
        this.albumDao = new AlbumDao();
    }

    /**
     * {@inheritDoc}
     *
     * @return total number of artist documents.
     */
    @Override
    public long getTotalNum() {
        return artistDao.getTotalNum();
    }

    /**
     * {@inheritDoc}
     *
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage() {
        return getArtistsPage(FIRST_PAGE_NUM);
    }

    /**
     * {@inheritDoc}
     *
     * @param page specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *             first page will be returned.
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage(Long page) {
        return getArtistsPage(ARTISTS_PER_PAGE_DEFAULT, page, null, null);
    }

    /**
     * {@inheritDoc}
     *
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage(String order, List<String> orderFields) {
        return getArtistsPage(ARTISTS_PER_PAGE_DEFAULT, FIRST_PAGE_NUM, order, orderFields);
    }

    /**
     * {@inheritDoc}
     *
     * @param perPage     specifies number of artists per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage(Long perPage, Long page, String order, List<String> orderFields) {

        if (page == null) {
            page = FIRST_PAGE_NUM;
        }

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        if (perPage == null) {
            perPage = ARTISTS_PER_PAGE_DEFAULT;
        }

        if (perPage <= 0) {
            throw new IllegalArgumentException("Per page value must be greater than zero");
        }

        ResourceDto<ArtistDto> artistsPage = new ResourceDto<>();
        artistsPage.setPagination(getPaginationInfo(page, perPage));
        long offset = (page - 1) * perPage;

        SortOption[] sortOptions = null;
        if (order != null && orderFields != null && orderFields.size() > 0) {
            SortOption.Order sortOrder = SortOption.Order.valueOf(order.toUpperCase());
            sortOptions = new SortOption[]{new SortOption(sortOrder, orderFields)};
        }

        List<Artist> artists = artistDao.getList(offset, perPage, sortOptions, ARTIST_SHORT_INFO_FIELDS);
        List<ArtistDto> artistDtoList = artists.stream()
                .map(this::artistToDto)
                .collect(Collectors.toList());

        artistsPage.setResults(artistDtoList);
        return artistsPage;
    }

    /**
     * {@inheritDoc}
     *
     * @param id artist's identifier.
     * @return artist with the specified identifier.
     */
    public ArtistDto getById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        Artist artist = artistDao.getById(id);
        ArtistDto artistDto = artistToDto(artist);

        List<AlbumDto> albumsDtoList = artist.getAlbumsIds().stream()
                .map(albumId -> albumDao.getById(albumId, "_id", "name", "cover_image_url", "released_date"))
                .map(this::albumToDto)
                .collect(Collectors.toList());

        artistDto.setAlbums(albumsDtoList);

        return artistDto;
    }

    /**
     * {@inheritDoc}
     *
     * @param id identifier of artist which will be deleted.
     */
    @Override
    public void deleteArtistById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        artistDao.deleteById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @param artist contains artist info.
     * @return created artist.
     */
    @Override
    public ArtistDto createArtist(Artist artist) {

        if (artist == null) {
            throw new IllegalArgumentException("Artist can not be null");
        }

        String id = UUID.randomUUID().toString();
        artist.setId(id);

        return artistToDto(artistDao.create(artist));
    }

    /**
     * {@inheritDoc}
     *
     * @param artist artist which will be updated. Note, that artist's id must be set, otherwise
     *               {@link IllegalArgumentException} will be thrown.
     * @return updated artist.
     */
    @Override
    public ArtistDto updateArtist(Artist artist) {

        if (artist == null || artist.getId() == null || artist.getId().isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        return artistToDto(artistDao.update(artist.getId(), artist));
    }

    /**
     * {@inheritDoc}
     *
     * @param id     identifier of artist which will be updated.
     * @param artist artist which will be updated.
     * @return updated artist.
     */
    @Override
    public ArtistDto updateArtist(String id, Artist artist) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        if (artist == null) {
            throw new IllegalArgumentException("Artist can not be null");
        }

        return artistToDto(artistDao.update(id, artist));
    }

    private ArtistDto artistToDto(Artist artist) {
        ArtistDto artistDto = new ArtistDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(artistDto, artist);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create artist Data Transfer Object", e);
        }

        return artistDto;
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
